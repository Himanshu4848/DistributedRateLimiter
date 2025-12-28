package com.ratelimiter.config;

import com.ratelimiter.interceptor.RateLimitInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration to register the rate limit interceptor
 * This tells Spring to use our interceptor for all requests
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    /**
     * Register interceptors
     * This method is called by Spring automatically
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("Registering RateLimitInterceptor for all API endpoints");

        registry.addInterceptor(rateLimitInterceptor)
                // Apply to these patterns
                .addPathPatterns(
                        "/api/**",        // All API endpoints
                        "/public/**"      // Public endpoints
                )
                // Exclude these patterns (no rate limiting)
                .excludePathPatterns(
                        "/actuator/**",   // Health checks, metrics
                        "/swagger-ui/**", // Swagger UI
                        "/v3/api-docs/**",// OpenAPI docs
                        "/error"          // Error page
                );

        log.info("RateLimitInterceptor registered successfully");
    }
}