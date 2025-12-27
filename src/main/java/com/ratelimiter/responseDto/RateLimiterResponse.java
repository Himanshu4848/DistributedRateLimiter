package com.ratelimiter.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimiterResponse {

    private boolean allowed;
    private long limit;
    private long remaining;
    private long resetAt;
    private String identifier;
    private String reason;

    public static RateLimiterResponse allowed(long limit, long remaining, long resetAt) {
        return RateLimiterResponse.builder()
                .allowed(true)
                .limit(limit)
                .remaining(remaining)
                .resetAt(resetAt)
                .build();
    }

    public static RateLimiterResponse blocked(long limit, long resetAt, String reason) {
        return RateLimiterResponse.builder()
                .allowed(false)
                .limit(limit)
                .remaining(0)
                .resetAt(resetAt)
                .reason(reason)
                .build();
    }

    public long getRetryAfterSeconds() {
        long now = Instant.now().getEpochSecond();
        return Math.max(0, resetAt - now);
    }

}
