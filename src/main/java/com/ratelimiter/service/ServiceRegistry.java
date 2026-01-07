package com.ratelimiter.service;

import com.ratelimiter.requestDto.ServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ServiceRegistry {

    private final Map<String, ServiceConfig> registeredServices = new ConcurrentHashMap<>();

    public void registerService(ServiceConfig serviceConfig) {
        if (serviceConfig == null || serviceConfig.getName() == null) {
            throw new IllegalArgumentException("Service configuration and service name cannot be null");
        }
        registeredServices.put(serviceConfig.getName(), serviceConfig);
        log.info("Service registered: {} | Path: {} | Target: {} | Per-User Limit: {} | Global Limit: {}",
                serviceConfig.getName(),
                serviceConfig.getPathPrefix(),
                serviceConfig.getTargetUrl(),
                serviceConfig.getPerUserLimit(),
                serviceConfig.getGlobalLimit());
    }

    /**
     * Get a registered service by service name
     */
    public ServiceConfig getService(String serviceName) {
        ServiceConfig config = registeredServices.get(serviceName);
        if (config == null) {
            throw new RuntimeException("Service not found: " + serviceName);
        }
        return config;
    }

    public ServiceConfig findServiceByPath(String requestPath) {
        for (ServiceConfig config : registeredServices.values()) {
            if (config.matches(requestPath)) {
                return config;
            }
        }
        throw new RuntimeException("No service found for path: " + requestPath);
    }

    public boolean isServiceRegistered(String serviceName) {
        return registeredServices.containsKey(serviceName);
    }

    /**
     * Get all registered services
     */
    public List<ServiceConfig> getAllServices() {
        return new ArrayList<>(registeredServices.values());
    }

    /**
     * Update an existing service configuration
     */
    public void updateService(String serviceName, ServiceConfig serviceConfig) {
        if (!isServiceRegistered(serviceName)) {
            throw new RuntimeException("Service not found: " + serviceName);
        }
        registeredServices.put(serviceName, serviceConfig);
        log.info("Service updated: {}", serviceName);
    }

    /**
     * Unregister a service
     */
    public void unregisterService(String serviceName) {
        if (registeredServices.remove(serviceName) != null) {
            log.info("Service unregistered: {}", serviceName);
        } else {
            throw new RuntimeException("Service not found: " + serviceName);
        }
    }

    /**
     * Get count of registered services
     */
    public int getServiceCount() {
        return registeredServices.size();
    }

    /**
     * Clear all registered services
     */
    public void clearAllServices() {
        registeredServices.clear();
        log.info("All services cleared from registry");
    }
}