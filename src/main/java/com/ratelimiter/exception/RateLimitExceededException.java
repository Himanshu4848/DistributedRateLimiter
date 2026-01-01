package com.ratelimiter.exception;

import com.ratelimiter.responseDto.RateLimiterResponse;
import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {

    private final RateLimiterResponse rateLimiterResponse;

    public RateLimitExceededException(String message, RateLimiterResponse rateLimiterResponse) {
        super(message);
        this.rateLimiterResponse = rateLimiterResponse;
    }

    public RateLimitExceededException(RateLimiterResponse rateLimiterResponse) {
        super("Rate limit exceeded");
        this.rateLimiterResponse = rateLimiterResponse;
    }
}