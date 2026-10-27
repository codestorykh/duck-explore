package com.duck.explore.idempotency.service;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

public class RedisIdempotencyStore {

    private final StringRedisTemplate redisTemplate;
    private final String prefix;

    public RedisIdempotencyStore(StringRedisTemplate redisTemplate, String prefix) {
        this.redisTemplate = redisTemplate;
        this.prefix = prefix;
    }

    /**
     * Atomically acquires the lock in Redis using SETNX + EXPIRE.
     */
    public boolean tryAcquire(String rawKey, Duration ttl) {
        String fullKey = buildKey(rawKey);
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(fullKey, "IN_PROGRESS", ttl);
        return Boolean.TRUE.equals(acquired);
    }

    /**
     * Releases the lock if the downstream transaction threw an exception and rolled back,
     * allowing the client to retry safely.
     */
    public void release(String rawKey) {
        redisTemplate.delete(buildKey(rawKey));
    }

    private String buildKey(String rawKey) {
        return prefix + ":" + rawKey;
    }
}