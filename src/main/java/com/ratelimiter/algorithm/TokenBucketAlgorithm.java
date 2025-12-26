package com.ratelimiter.algorithm;


import com.ratelimiter.responseDto.RateLimiterResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class TokenBucketAlgorithm {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    private static final String LUA_SCRIPT =
            // Get parameters
            "local key = KEYS[1] " +
                    "local capacity = tonumber(ARGV[1]) " +
                    "local refill_rate = tonumber(ARGV[2]) " +
                    "local current_time = tonumber(ARGV[3]) " +

                    // Get current state from Redis
                    "local tokens = tonumber(redis.call('HGET', key, 'tokens')) " +
                    "local last_refill = tonumber(redis.call('HGET', key, 'last_refill')) " +

                    // Initialize on first request
                    "if tokens == nil then " +
                    "  tokens = capacity " +
                    "  last_refill = current_time " +
                    "  redis.call('HSET', key, 'tokens', tokens) " +
                    "  redis.call('HSET', key, 'last_refill', last_refill) " +
                    "  redis.call('EXPIRE', key, 3600) " +  // Auto-delete after 1 hour of inactivity
                    "end " +

                    // Calculate tokens to add based on time passed
                    "local time_passed = math.max(0, current_time - last_refill) " +
                    "local tokens_to_add = time_passed * refill_rate " +
                    "tokens = math.min(capacity, tokens + tokens_to_add) " +
                    "last_refill = current_time " +

                    // Try to consume 1 token
                    "if tokens >= 1 then " +
                    "  tokens = tokens - 1 " +
                    "  redis.call('HSET', key, 'tokens', tokens) " +
                    "  redis.call('HSET', key, 'last_refill', last_refill) " +
                    "  local tokens_needed = capacity - tokens " +
                    "  local seconds_until_full = tokens_needed / refill_rate " +
                    "  local reset_at = current_time + seconds_until_full " +
                    "  return {1, math.floor(tokens), math.floor(reset_at)} " +
                    "else " +
                    "  redis.call('HSET', key, 'last_refill', last_refill) " +
                    "  local tokens_needed = 1 - tokens " +
                    "  local seconds_until_available = tokens_needed / refill_rate " +
                    "  local reset_at = current_time + seconds_until_available " +
                    "  return {0, 0, math.floor(reset_at)} " +
                    "end";


    public RateLimiterResponse isAllowed(String key, int limit, int windowSeconds) {
        try {
            // Calculate refill rate: capacity / window
            // Example: 10 requests / 60 seconds = 0.1667 tokens per second
            double refillRate = (double) limit / windowSeconds;
            // Current time in seconds
            long currentTime = System.currentTimeMillis() / 1000;
            // Execute Lua script atomically in Redis
            DefaultRedisScript<List> script = new DefaultRedisScript<>(LUA_SCRIPT, List.class);

            List<Long> result = redisTemplate.execute(
                    script,
                    Collections.singletonList(key),
                    String.valueOf(limit),
                    String.valueOf(refillRate),
                    String.valueOf(currentTime)
            );
            // Parse result from Lua script
            if (result == null || result.size() != 3) {
                log.error("Unexpected Redis response for key: {}", key);
                // Fail open - allow request if Redis has issues
                return RateLimiterResponse.allowed(limit, limit, currentTime + windowSeconds);
            }
            long allowed = result.get(0);
            long remaining = result.get(1);
            long resetAt = result.get(2);

            if (allowed == 1) {
                log.debug("✓ Request allowed - Key: {}, Remaining: {}", key, remaining);
                return RateLimiterResponse.allowed(limit, remaining, resetAt);
            } else {
                log.info("✗ Request blocked - Key: {}, Reset at: {}", key, resetAt);
                return RateLimiterResponse.blocked(limit, resetAt, "Rate limit exceeded");
            }

        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            // Fail open - allow request on error
            long currentTime = System.currentTimeMillis() / 1000;
            return RateLimiterResponse.allowed(limit, limit, currentTime + windowSeconds);
        }
    }

    public void reset(String key) {
        try {
            redisTemplate.delete(key);
            log.info("Reset rate limit for key: {}", key);
        } catch (Exception e) {
            log.error("Error resetting rate limit for key: {}", key, e);
        }
    }
}