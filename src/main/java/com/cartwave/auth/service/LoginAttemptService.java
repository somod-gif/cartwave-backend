package com.cartwave.auth.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 15;

    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<String, Instant> lockUntil = new ConcurrentHashMap<>();

    public boolean isLocked(String key) {
        Instant until = lockUntil.get(key);
        return until != null && until.isAfter(Instant.now());
    }

    public void onFailure(String key) {
        int next = attempts.getOrDefault(key, 0) + 1;
        attempts.put(key, next);
        if (next >= MAX_ATTEMPTS) {
            lockUntil.put(key, Instant.now().plusSeconds(LOCK_MINUTES * 60));
        }
    }

    public void onSuccess(String key) {
        attempts.remove(key);
        lockUntil.remove(key);
    }
}
