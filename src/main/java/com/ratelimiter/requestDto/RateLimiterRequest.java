package com.ratelimiter.requestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimiterRequest {
    private String endpoint;
    private String identifier;
    private String ipAddress;
    private String userId;
    private String apiKey;
    private Instant timestamp;
}
