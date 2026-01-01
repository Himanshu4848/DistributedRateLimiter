package com.ratelimiter.interceptor;

import com.ratelimiter.exception.RateLimitExceededException;
import com.ratelimiter.requestDto.RateLimiterRequest;
import com.ratelimiter.responseDto.RateLimiterResponse;
import com.ratelimiter.service.RateLimiterService;
import com.ratelimiter.utils.IdentifierExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;

/**
 * Interceptor that applies rate limiting to all incoming requests
 * This runs BEFORE controllers - it can block requests before they reach your API
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private IdentifierExtractor identifierExtractor;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,@NonNull Object handler) throws Exception {

        try {
            RateLimiterRequest rateLimiterRequest = buildRateLimiterRequest(request);
            RateLimiterResponse result = rateLimiterService.checkRateLimit(rateLimiterRequest);
//            addRateLimitHeaders(response, result);
            return true;

        } catch (RateLimitExceededException ex) {
            RateLimiterResponse result = ex.getResponse();
           // addRateLimitHeaders(response, result);
            handleRateLimitExceeded(response, result);
            return false;
        }
    }

    /**
     * Build RateLimiterRequest from HTTP request
     */
    private RateLimiterRequest buildRateLimiterRequest(HttpServletRequest request) {
        return RateLimiterRequest.builder()
                .userId(identifierExtractor.extractUserId(request))
                .ipAddress(identifierExtractor.extractIpAddress(request))
                .apiKey(identifierExtractor.extractApiKey(request))
                .identifier(identifierExtractor.extractPrimaryIdentifier(request))
                .endpoint(identifierExtractor.extractEndpoint(request))
                .timestamp(Instant.now())
                .build();
    }

    private boolean shouldSkipRateLimiting(String endpoint) {
        // Skip actuator endpoints (health checks, metrics)
        if (endpoint.startsWith("/actuator")) {
            return true;
        }
        if (endpoint.startsWith("/swagger-ui") ||
                endpoint.startsWith("/v3/api-docs") ||
                endpoint.equals("/swagger-ui.html")) {
            return true;
        }
        if (endpoint.startsWith("/static") ||
                endpoint.startsWith("/css") ||
                endpoint.startsWith("/js") ||
                endpoint.startsWith("/images")) {
            return true;
        }
        return endpoint.equals("/error");
    }

    private void addRateLimitHeaders(HttpServletResponse response, RateLimiterResponse result) {

        // X-RateLimit-Limit: Maximum requests allowed
        response.addHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
        // X-RateLimit-Remaining: How many requests left
        response.addHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
        // X-RateLimit-Reset: When the limit resets (Unix timestamp)
        response.addHeader("X-RateLimit-Reset", String.valueOf(result.getResetAt()));
        // If blocked, add Retry-After header (how long to wait)
        if (!result.isAllowed()) {
            long retryAfter = result.getRetryAfterSeconds();
            response.addHeader("Retry-After", String.valueOf(retryAfter));
        }
    }

    private void handleRateLimitExceeded(HttpServletResponse response,
                                         RateLimiterResponse result) throws Exception {

        response.setStatus(429);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        long now = System.currentTimeMillis() / 1000;
        long retryAfterSeconds = Math.max(0, result.getResetAt() - now);

        String errorJson = String.format("""
        {
            "allowed": %b,
            "message": "%s",
            "retryAfterSeconds": %d
        }
        """,
                result.isAllowed(),
                result.getReason(),
                retryAfterSeconds
        );

        response.getWriter().write(errorJson);
        response.getWriter().flush();
    }
}