package com.duck.explore.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "external.gateway")
public class ExternalGatewayProperties {

    private String baseUrl = "https://api.external-gateway.com";
    private Timeouts timeouts = new Timeouts();
    private Pool pool = new Pool();
    private Retry retry = new Retry();

    @Getter
    @Setter
    public static class Timeouts {
        private int connectTimeoutMs = 1500;
        private int socketTimeoutMs = 3000;
        private int connectionRequestTimeoutMs = 500;
    }

    @Getter
    @Setter
    public static class Pool {
        private int maxTotal = 200;
        private int defaultMaxPerRoute = 50;
        private int idleEvictionSeconds = 30;
    }

    @Getter
    @Setter
    public static class Retry {
        private int maxAttempts = 3;
        private long delay = 500;
        private double multiplier = 2.0;
        private boolean random = true;
    }
}