package com.ratelimiter.requestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceConfig {

    private String name;
    private String pathPrefix;
    private String targetUrl;
    private boolean stripPrefix;
    private long perUserLimit;
    private long globalLimit;

    public boolean matches(String requestPath) {
        if (requestPath == null || pathPrefix == null) {
            return false;
        }
        return requestPath.startsWith(pathPrefix);
    }

    public String transformPath(String requestPath) {
        if (stripPrefix && requestPath.startsWith(pathPrefix)) {
            String transformed = requestPath.substring(pathPrefix.length());
            if (!transformed.startsWith("/")) {
                transformed = "/" + transformed;
            }
            return transformed;
        }
        return requestPath;
    }
}