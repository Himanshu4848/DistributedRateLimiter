package com.ratelimiter.controller;

import com.ratelimiter.service.ProxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/gateway")
public class GatewayController {

    @Autowired
    private ProxyService proxyService;

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.DELETE,
            RequestMethod.PATCH, RequestMethod.OPTIONS})
    public Mono<ResponseEntity<Object>> handleRequest(@RequestBody(required = false) Object requestBody,
                                                      HttpServletRequest httpRequest) {
        try {
            // Extract the full request path (remove /gateway prefix)
            String fullPath = httpRequest.getRequestURI();
            String requestPath = fullPath.replace("/gateway", "");

            if (requestPath.isEmpty()) {
                requestPath = "/";
            }
            String httpMethod = httpRequest.getMethod();
            log.info("Gateway Request - Method: {}, Path: {}", httpMethod, requestPath);
            // (Rate limit already checked by interceptor)
            return proxyService.proxyRequest(requestPath, httpMethod, requestBody);

        } catch (Exception e) {
            log.error("Error in gateway controller: {}", e.getMessage(), e);
            return Mono.just(ResponseEntity
                    .status(500)
                    .body((Object) "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Gateway is healthy");
    }
}