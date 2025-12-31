package com.ratelimiter.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.ratelimiter.constants.CommonConstants.BASE_URL;
import static com.ratelimiter.constants.CommonConstants.GET_USER;
import static com.ratelimiter.constants.CommonConstants.GET_USER_PRODUCTS;
import static com.ratelimiter.constants.CommonConstants.TEST_URL;

@Slf4j
@RestController
@RequestMapping(BASE_URL)
public class testController {

    @GetMapping(TEST_URL)
    public Map<String, Object> test() {
        log.info("Test endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello! Rate limiter is working");
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    @GetMapping(GET_USER_PRODUCTS)
    public Map<String, Object> getProducts() {
        log.info("Products endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("products", new String[]{"Product 1", "Product 2", "Product 3"});
        response.put("count", 3);
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    @GetMapping(GET_USER)
    public Map<String, Object> getUsers() {
        log.info("Users endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("users", new String[]{"Alice", "Bob", "Carol"});
        response.put("count", 3);
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    @PostMapping("/data")
    public Map<String, Object> postData(@RequestBody(required = false) Map<String, Object> data) {
        log.info("Data endpoint called with: {}", data);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Data received");
        response.put("receivedData", data);
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    @GetMapping("/expensive")
    public Map<String, Object> expensiveOperation() throws InterruptedException {
        log.info("Expensive operation called");
        Thread.sleep(100);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Expensive operation completed");
        response.put("timestamp", Instant.now().toString());
        return response;
    }
}