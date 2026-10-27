package com.duck.explore.idempotency.aop;

import com.duck.explore.idempotency.annotation.Idempotent;
import com.duck.explore.idempotency.configuration.properties.IdempotencyProperties;
import com.duck.explore.idempotency.exception.DuplicateTransactionException;
import com.duck.explore.idempotency.service.IdempotencyKeyResolver;
import com.duck.explore.idempotency.service.RedisIdempotencyStore;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@Aspect
public class IdempotencyAspect {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyAspect.class);

    private final RedisIdempotencyStore redisIdempotencyStore;
    private final IdempotencyKeyResolver idempotencyKeyResolver;
    private final IdempotencyProperties idempotencyProperties;

    public IdempotencyAspect(RedisIdempotencyStore redisIdempotencyStore,
                             IdempotencyKeyResolver idempotencyKeyResolver,
                             IdempotencyProperties idempotencyProperties) {
        this.redisIdempotencyStore = redisIdempotencyStore;
        this.idempotencyKeyResolver = idempotencyKeyResolver;
        this.idempotencyProperties = idempotencyProperties;
    }

    @Around("@annotation(idempotent)")
    public Object enforceIdempotency(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String resolvedKey = idempotencyKeyResolver.resolveKey(joinPoint, idempotent.key(), idempotencyProperties.getHeaderName());
        Duration ttl = Duration.of(idempotent.ttl(), idempotent.timeUnit().toChronoUnit());

        boolean acquired = redisIdempotencyStore.tryAcquire(resolvedKey, ttl);
        if (!acquired) {
            log.warn("[IDEMPOTENCY REJECTED] Duplicate request detected for key: {}", resolvedKey);
            throw new DuplicateTransactionException(resolvedKey);
        }

        log.debug("[IDEMPOTENCY ACQUIRED] Key '{}' locked for {}s", resolvedKey, ttl.toSeconds());

        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            // Release lock on failure so legitimate retries can proceed
            log.warn("[IDEMPOTENCY RELEASED] Execution failed for key '{}', releasing lock", resolvedKey);
            redisIdempotencyStore.release(resolvedKey);
            throw ex;
        }
    }
}