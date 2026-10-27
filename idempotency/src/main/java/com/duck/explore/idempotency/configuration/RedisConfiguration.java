package com.duck.explore.idempotency.configuration;

import com.duck.explore.idempotency.configuration.properties.RedisProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableRedisRepositories
public class RedisConfiguration {

    @Bean
    public RedisProperties redisProperties() {
        return new RedisProperties();
    }

    @Bean
    public JedisConnectionFactory connectionFactory(RedisProperties redisProperties) {

        if (StringUtils.hasText(redisProperties.getSentinelMaster())) {
            RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
            sentinelConfig.master(redisProperties.getSentinelMaster());
            for (String node : redisProperties.getSentinelNodes().split(",")) {
                String[] split = node.split(":");
                sentinelConfig.sentinel(split[0], Integer.parseInt(split[1]));
            }
            return new JedisConnectionFactory(sentinelConfig);
        } else {
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
            configuration.setHostName(redisProperties.getHost());
            configuration.setPort(redisProperties.getPort());

            if (redisProperties.isEnabledRedisPassword()
                    && StringUtils.hasText(redisProperties.getPassword())) {
                configuration.setPassword(redisProperties.getPassword());
            }
            // 2. Configure Jedis pool settings (requires spring-boot-starter-data-redis with commons-pool2)
           /* JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(32);
            poolConfig.setMaxIdle(8);
            poolConfig.setMinIdle(2);*/

            if (redisProperties.isEnabledSSL()) {
                JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder()
                        .useSsl()
                        .and()
                        .usePooling()
                        .build();
                return new JedisConnectionFactory(configuration, jedisClientConfiguration);
            }

            return new JedisConnectionFactory(configuration);
        }
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        // Spring injects this template directly into your RedisIdempotencyStore!
        return new StringRedisTemplate(jedisConnectionFactory);
    }

}
