package com.ratelimiter.controller;

import com.ratelimiter.requestDto.RateLimiterRequest;
import com.ratelimiter.service.RateLimiterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.ratelimiter.constants.CommonConstants.ADMIN_BASE_URL;
import static com.ratelimiter.constants.CommonConstants.HEALTH_CHECK_URL;
import static com.ratelimiter.constants.CommonConstants.RESET_GLOBAL_LIMIT;
import static com.ratelimiter.constants.CommonConstants.RESET_USER_LIMIT;

@Slf4j
@RestController
@RequestMapping(ADMIN_BASE_URL)
public class AdminController {

    @Autowired
    private RateLimiterService rateLimiterService;

    @PostMapping(RESET_USER_LIMIT)
    public Map<String, Object> resetUserRateLimit(@RequestBody RateLimiterRequest request) {
        log.info("Resetting user rate limit for: {}", request.getIdentifier());
        rateLimiterService.resetUserRateLimit(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User rate limit reset successfully");
        response.put("identifier", request.getIdentifier());
        response.put("endpoint", request.getEndpoint());
        return response;
    }

    @Transactional
    @PostMapping(RESET_GLOBAL_LIMIT)
    public Map<String, Object> resetGlobalRateLimit(@RequestParam String endpoint) {
        log.info("Resetting global rate limit for endpoint: {}", endpoint);
        rateLimiterService.resetGlobalRateLimit(endpoint);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Global rate limit reset successfully");
        response.put("endpoint", endpoint);
        return response;
    }

    @GetMapping(HEALTH_CHECK_URL)
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "rate-limiter");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}