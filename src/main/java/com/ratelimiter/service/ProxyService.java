package com.ratelimiter.service;

import com.ratelimiter.requestDto.ServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.apache.tomcat.util.http.Method.DELETE;
import static org.apache.tomcat.util.http.Method.GET;
import static org.apache.tomcat.util.http.Method.POST;
import static org.apache.tomcat.util.http.Method.PUT;

@Slf4j
@Service
public class ProxyService {

    @Autowired
    private ServiceRegistry serviceRegistry;

    @Autowired
    private WebClient webClient;

    /**
     * Proxy request to backend service
     * Rate limiting already checked by RateLimitInterceptor
     */
    public Mono<ResponseEntity<Object>> proxyRequest(String requestPath,
                                                     String httpMethod,
                                                     Object requestBody) {
        try {
            // Step 1: Find the target service based on path
            ServiceConfig targetService = serviceRegistry.findServiceByPath(requestPath);
            log.info("Found target service: {} for path: {}", targetService.getName(), requestPath);

            // Step 2: Transform the request path (strip prefix)
            String transformedPath = targetService.transformPath(requestPath);

            // Step 3: Build full target URL
            String fullTargetUrl = targetService.getTargetUrl() + transformedPath;
            log.info("Forwarding request to: {}", fullTargetUrl);

            // Step 4: Forward request to backend service
            return forwardRequest(fullTargetUrl, httpMethod, requestBody);

        } catch (RuntimeException e) {
            log.error("Error processing request for path: {}, Error: {}", requestPath, e.getMessage());
            return Mono.just(ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body((Object) "Service not found: " + e.getMessage()));
        }
    }

    private Mono<ResponseEntity<Object>> forwardRequest(String targetUrl,
                                                        String httpMethod,
                                                        Object requestBody) {
        try {
            return switch (httpMethod.toUpperCase()) {
                case GET -> webClient.get()
                        .uri(targetUrl)
                        .retrieve()
                        .toEntity(Object.class);
                case POST -> webClient.post()
                        .uri(targetUrl)
                        .bodyValue(requestBody)
                        .retrieve()
                        .toEntity(Object.class);
                case PUT -> webClient.put()
                        .uri(targetUrl)
                        .bodyValue(requestBody)
                        .retrieve()
                        .toEntity(Object.class);
                case DELETE -> webClient.delete()
                        .uri(targetUrl)
                        .retrieve()
                        .toEntity(Object.class);
                default -> Mono.just(ResponseEntity
                        .status(HttpStatus.METHOD_NOT_ALLOWED)
                        .body((Object) "HTTP method not supported: " + httpMethod));
            };
        } catch (Exception e) {
            log.error("Failed to forward request to {}: {}", targetUrl, e.getMessage());
            return Mono.just(ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body((Object) "Service unavailable"));
        }
    }
}