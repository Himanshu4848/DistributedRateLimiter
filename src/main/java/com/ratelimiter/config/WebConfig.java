package com.ratelimiter.config;

import com.ratelimiter.interceptor.RateLimitInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("Registering RateLimitInterceptor for all API endpoints");

        registry.addInterceptor(rateLimitInterceptor)
                // Apply to these patterns
                .addPathPatterns(
                        "/api/**"
                )
                // Exclude these patterns (no rate limiting)
                .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**", "/error");
        log.info("RateLimitInterceptor registered successfully");
    }
}