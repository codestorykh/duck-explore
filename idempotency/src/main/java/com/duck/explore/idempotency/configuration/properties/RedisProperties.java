package com.duck.explore.idempotency.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ToString
@Getter
@Setter
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {

    private String host = "http://localhost";

    private int port = 6379;

    private String password;

    private String sentinelMaster;

    private String sentinelNodes;

    private boolean enabledSSL = false;

    private boolean enabledRedisPassword = true;

    private int timeout = 30000; // milliseconds

    private int connectionTimeout = 5000; // milliseconds

    private int maxWait = 10000; // milliseconds

    private int timeBetweenEvictionRuns = 120000; // milliseconds

    private String messageResponseKey;
}
