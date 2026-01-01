package com.ratelimiter.service;

import com.ratelimiter.algorithm.TokenBucketAlgorithm;
import com.ratelimiter.exception.RateLimitExceededException;
import com.ratelimiter.requestDto.RateLimiterRequest;
import com.ratelimiter.responseDto.RateLimiterResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.ratelimiter.constants.CommonConstants.DEFAULT_USER_KEY;
import static com.ratelimiter.constants.CommonConstants.GLOBAL_REDIS_BASE_KEY;
import static com.ratelimiter.constants.CommonConstants.REPLACEMENT;
import static com.ratelimiter.constants.CommonConstants.SANITIZE_REGEX;
import static com.ratelimiter.constants.CommonConstants.USER_REDIS_BASE_KEY;


@Slf4j
@Service
public class RateLimiterService {

    @Autowired
    private TokenBucketAlgorithm tokenBucketAlgorithm;

    // Configuration from application.properties
    @Value("${rate-limiter.enabled:true}")
    private boolean rateLimiterEnabled;

    @Value("${rate-limiter.default-limit:10}")
    private int defaultLimit;

    @Value("${rate-limiter.default-window-seconds:60}")
    private int defaultWindowSeconds;

    @Value("${rate-limiter.global.enabled:false}")
    private boolean globalRateLimitEnabled;

    @Value("${rate-limiter.global.limit:1000}")
    private int globalLimit;

    @Value("${rate-limiter.global.window-seconds:60}")
    private int globalWindowSeconds;
    /**
     * Main method to check rate limit
     *
     * Flow:
     * 1. Check if rate limiter is enabled
     * 2. Check global rate limit (if enabled)
     * 3. Check per-user rate limit
     */
    public RateLimiterResponse checkRateLimit(RateLimiterRequest rateLimiterRequest) {

        if (!rateLimiterEnabled) {
            log.info("Rate limiter is disabled");
            return createAllowedResponse();
        }
        if (globalRateLimitEnabled) {

            RateLimiterResponse globalResult = checkGlobalRateLimit(rateLimiterRequest.getEndpoint());
            if (!globalResult.isAllowed()) {
                log.warn("Global rate limit exceeded");
                throw new RateLimitExceededException(globalResult);
            }
        }
        RateLimiterResponse userResult = checkUserRateLimit(rateLimiterRequest);
        if (!userResult.isAllowed()) {
            throw new RateLimitExceededException(userResult);
        }
        userResult.setIdentifier(rateLimiterRequest.getIdentifier());
        return userResult;
    }

    private RateLimiterResponse checkGlobalRateLimit(String endpoint) {
        String apiEndpoint = sanitize(endpoint);
        String globalKey = GLOBAL_REDIS_BASE_KEY + apiEndpoint;
        log.debug("Checking global rate limit - Key: {}, Limit: {}/{}s",
                globalKey, globalLimit, globalWindowSeconds);

        return tokenBucketAlgorithm.isAllowed(
                globalKey,
                globalLimit,
                globalWindowSeconds
        );
    }

    private RateLimiterResponse checkUserRateLimit(RateLimiterRequest rateLimiterRequest) {
        // Build key specific to this user
        String userKey = buildUserKey(rateLimiterRequest);
        log.debug("Checking user rate limit - Key: {}, Limit: {}/{}s", userKey, defaultLimit, defaultWindowSeconds);

        return tokenBucketAlgorithm.isAllowed(
                userKey,
                defaultLimit,
                defaultWindowSeconds
        );
    }

    private String buildUserKey(RateLimiterRequest rateLimiterRequest) {
        String identifier = rateLimiterRequest.getIdentifier();
        String endPoint = sanitize(rateLimiterRequest.getEndpoint());
        if (identifier == null || identifier.isEmpty()) {
            // Fallback to IP if no identifier
            identifier = "ip:" + rateLimiterRequest.getIpAddress();
        }
        return USER_REDIS_BASE_KEY + endPoint + ":" + sanitize(identifier);
    }

    private String sanitize(String input) {
        if (input == null) {
            return DEFAULT_USER_KEY;
        }
        return input.replaceAll(SANITIZE_REGEX, REPLACEMENT);
    }

    private RateLimiterResponse createAllowedResponse() {
        return RateLimiterResponse.builder()
                .allowed(true)
                .limit(Integer.MAX_VALUE)
                .remaining(Integer.MAX_VALUE)
                .resetAt(System.currentTimeMillis() / 1000 + 3600)
                .build();
    }

    public void resetUserRateLimit(RateLimiterRequest rateLimiterRequest) {
        String endPoint = sanitize(rateLimiterRequest.getEndpoint());
        String key = USER_REDIS_BASE_KEY + endPoint + ":" + sanitize(rateLimiterRequest.getIdentifier());
        tokenBucketAlgorithm.reset(key);
        log.info("Reset user rate limit for: {}", rateLimiterRequest.getUserId());
    }

    public void resetGlobalRateLimit(String apiEndPoint) {
        String apiEndpoint = sanitize(apiEndPoint);
        String globalKey = GLOBAL_REDIS_BASE_KEY + apiEndpoint;
        tokenBucketAlgorithm.reset(globalKey);
        log.info("Reset global rate limit for key:{}",globalKey);
    }
}