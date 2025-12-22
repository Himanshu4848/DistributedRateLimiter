package com.ratelimiter.responseDto;

import lombok.Data;

@Data
public class RateLimiterResponse {

    private boolean allowed;
    private long limit;
    private long remaining;
    private long resetAt;
    private String identifier;
    private String reason;
}
