package com.ratelimiter.constants;

public class CommonConstants {
    public static final String DEFAULT_USER_KEY = "unknown";
    public static final String SANITIZE_REGEX = "[^a-zA-Z0-9_:.-]";
    public static final String REPLACEMENT = "_";
    public static final String USER_REDIS_BASE_KEY="rate_limit:user:";
    public static final String GLOBAL_REDIS_BASE_KEY="rate_limit:global:";
    public static final String BASE_URL="/api/v1";
    public static final String TEST_URL="/test";
    public static final String GET_USER_PRODUCTS="/products";
    public static final String GET_USER="/users";
    public static final String ADMIN_BASE_URL="/admin/rate-limiter";
    public static final String RESET_USER_LIMIT="/reset/user";
    public static final String RESET_GLOBAL_LIMIT="/reset/global";
    public static final String HEALTH_CHECK_URL="/health";
}
