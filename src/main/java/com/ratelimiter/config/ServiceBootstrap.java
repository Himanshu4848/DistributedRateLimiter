package com.ratelimiter.config;

import com.ratelimiter.service.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServiceBootstrap implements ApplicationRunner {

    @Autowired
    private ServiceRegistry serviceRegistry;

    @Autowired
    private ServiceConfigProperties serviceConfigProperties;

    /**
     * Load services from application.yml into ServiceRegistry at application startup
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("========== Initializing Service Registry from application.yml ==========");

        try {
            // Get all services from application.yml and register them
            if (serviceConfigProperties.getServices() != null && !serviceConfigProperties.getServices().isEmpty()) {
                serviceConfigProperties.getServices().forEach((serviceName, serviceConfig) -> {
                    serviceRegistry.registerService(serviceConfig);
                });

                log.info("Successfully registered {} services from application.yml",
                        serviceRegistry.getServiceCount());
                logRegisteredServices();
            } else {
                log.warn("No services found in application.yml");
            }

        } catch (Exception e) {
            log.error("Error during service initialization: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize services", e);
        }
    }

    /**
     * Log all registered services for debugging
     */
    private void logRegisteredServices() {
        log.info("===================== Registered Services =====================");
        serviceRegistry.getAllServices().forEach(service ->
                log.info("Service: {} | Path: {} | Target: {} | Per-User: {} req/min | Global: {} req/sec",
                        service.getName(),
                        service.getPathPrefix(),
                        service.getTargetUrl(),
                        service.getPerUserLimit(),
                        service.getGlobalLimit())
        );
        log.info("==============================================================");
    }
}