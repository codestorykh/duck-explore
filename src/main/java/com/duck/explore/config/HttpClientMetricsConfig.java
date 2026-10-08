package com.duck.explore.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.pool.PoolStats;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientMetricsConfig {

    @Bean
    public MeterBinder httpClientPoolMetrics(PoolingHttpClientConnectionManager connectionManager) {
        return (MeterRegistry registry) -> {
            String poolName = "payment_gateway";

            // Active connections in-flight
            Gauge.builder("http.client.pool.leased", connectionManager, cm -> cm.getTotalStats().getLeased())
                    .description("Number of persistent connections currently leased to active requests")
                    .tag("pool", poolName)
                    .register(registry);

            // Available idle connections
            Gauge.builder("http.client.pool.available", connectionManager, cm -> cm.getTotalStats().getAvailable())
                    .description("Number of persistent connections idling and available in the pool")
                    .tag("pool", poolName)
                    .register(registry);

            // Blocked threads waiting for a connection lease (saturation indicator)
            Gauge.builder("http.client.pool.pending", connectionManager, cm -> cm.getTotalStats().getPending())
                    .description("Number of connection requests waiting for an available connection from the pool")
                    .tag("pool", poolName)
                    .register(registry);

            // Max configured pool size
            Gauge.builder("http.client.pool.max", connectionManager, cm -> cm.getTotalStats().getMax())
                    .description("Maximum number of persistent connections configured for this pool")
                    .tag("pool", poolName)
                    .register(registry);
        };
    }
}