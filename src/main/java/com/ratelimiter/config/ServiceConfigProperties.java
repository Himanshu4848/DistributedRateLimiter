package com.ratelimiter.config;

import com.ratelimiter.requestDto.ServiceConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Reads 'services' section from application.yml
 * Binds each service configuration to ServiceConfig objects
 */
@Data
@Component
@ConfigurationProperties()
public class ServiceConfigProperties {

    /**
     * Map of service configurations from application.yml
     * Key: service name (e.g., "user-service")
     * Value: ServiceConfig object with all service details
     */
    private Map<String, ServiceConfig> services;
}