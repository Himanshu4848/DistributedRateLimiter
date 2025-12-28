package com.ratelimiter.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility to extract various identifiers from HTTP requests
 * Used to determine who is making the request for rate limiting
 */
@Slf4j
@Component
public class IdentifierExtractor {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_CLIENT_IP"
    };

    public String extractIpAddress(HttpServletRequest request) {
        // Check proxy headers first
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                log.info("Extracted IP from header {}: {}", header, ip);
                return ip;
            }
        }

        String ip = request.getRemoteAddr();
        log.debug("Extracted IP from remote address: {}", ip);
        return ip;
    }

    public String extractUserId(HttpServletRequest request) {
        // Option 1: Check custom header (simple approach)
        String userId = request.getHeader("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            log.debug("Extracted user ID from X-User-Id header: {}", userId);
            return userId;
        }
//
//        // Option 2: Extract from Authorization header (JWT)
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            // TODO: Implement JWT decoding
//            log.debug("Authorization header present but JWT decoding not implemented");
//        }

        // Option 3: Check session
        if (request.getSession(false) != null) {
            Object userIdFromSession = request.getSession().getAttribute("userId");
            if (userIdFromSession != null) {
                log.debug("Extracted user ID from session: {}", userIdFromSession);
                return userIdFromSession.toString();
            }
        }
        log.debug("No user ID found in request");
        return null;
    }

    public String extractApiKey(HttpServletRequest request) {
        // Option 1: Header (recommended)
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            log.info("Extracted API key from header: {}****",
                    apiKey.substring(0, Math.min(4, apiKey.length())));
            return apiKey;
        }
        apiKey = request.getParameter("api_key");
        if (apiKey != null && !apiKey.isEmpty()) {
            log.debug("Extracted API key from query parameter");
            return apiKey;
        }

        log.debug("No API key found in request");
        return null;
    }

    public String extractSessionId(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            String sessionId = request.getSession().getId();
            log.debug("Extracted session ID: {}", sessionId);
            return sessionId;
        }
        return null;
    }

    public String extractPrimaryIdentifier(HttpServletRequest request) {
        String userId = extractUserId(request);
        if (userId != null) {
            return "user:" + userId;
        }
        String apiKey = extractApiKey(request);
        if (apiKey != null) {
            return "apikey:" + apiKey;
        }
        String sessionId = extractSessionId(request);
        if (sessionId != null) {
            return "session:" + sessionId;
        }
        String ip = extractIpAddress(request);
        return "ip:" + ip;
    }

    public String extractEndpoint(HttpServletRequest request) {
        return request.getRequestURI();
    }
}