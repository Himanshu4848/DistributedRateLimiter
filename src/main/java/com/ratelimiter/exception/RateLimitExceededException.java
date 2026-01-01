package com.ratelimiter.exception;

import com.ratelimiter.responseDto.RateLimiterResponse;
import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {

    private final RateLimiterResponse response;

    public RateLimitExceededException(RateLimiterResponse response) {
        this.response = response;
    }
}