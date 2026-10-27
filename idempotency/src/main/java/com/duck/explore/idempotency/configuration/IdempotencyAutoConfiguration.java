package com.duck.explore.idempotency.configuration;

import com.duck.explore.idempotency.aop.IdempotencyAspect;
import com.duck.explore.idempotency.configuration.properties.IdempotencyProperties;
import com.duck.explore.idempotency.service.IdempotencyKeyResolver;
import com.duck.explore.idempotency.service.RedisIdempotencyStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

@AutoConfiguration(after = DataRedisAutoConfiguration.class)
@ConditionalOnClass(StringRedisTemplate.class)
@ConditionalOnProperty(prefix = "enterprise.idempotency", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(IdempotencyProperties.class)
public class IdempotencyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyKeyResolver idempotencyKeyResolver() {
        return new IdempotencyKeyResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisIdempotencyStore redisIdempotencyStore(StringRedisTemplate redisTemplate,
                                                       IdempotencyProperties properties) {
        return new RedisIdempotencyStore(redisTemplate, properties.getPrefix());
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyAspect idempotencyAspect(RedisIdempotencyStore store,
                                               IdempotencyKeyResolver keyResolver,
                                               IdempotencyProperties properties) {
        return new IdempotencyAspect(store, keyResolver, properties);
    }
}