package com.ratelimiter.constants;

public class CommonConstants {
    public static final String DEFAULT_USER_KEY = "unknown";
    public static final String SANITIZE_REGEX = "[^a-zA-Z0-9_:.-]";
    public static final String REPLACEMENT = "_";
    public static final String USER_REDIS_BASE_KEY="rate_limit:user:";
    public static final String GLOBAL_REDIS_BASE_KEY="rate_limit:global:";
}
