package com.duck.explore.config;

import com.duck.explore.config.properties.ExternalGatewayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final ExternalGatewayProperties gatewayProperties;

    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(gatewayProperties.getTimeouts().getConnectTimeoutMs(), TimeUnit.MILLISECONDS))
                .setSocketTimeout(Timeout.of(gatewayProperties.getTimeouts().getSocketTimeoutMs(), TimeUnit.MILLISECONDS))
                // 1. Check if the connection is dead before leasing if idle for > 2 seconds
                .setValidateAfterInactivity(TimeValue.ofSeconds(2))
                .setTimeToLive(TimeValue.ofMinutes(5)) // Hard cap on total connection lifetime
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultConnectionConfig(connectionConfig);
        connectionManager.setMaxTotal(gatewayProperties.getPool().getMaxTotal());
        connectionManager.setDefaultMaxPerRoute(gatewayProperties.getPool().getDefaultMaxPerRoute());

        return connectionManager;
    }

    /**
     * 2. Custom Keep-Alive Strategy:
     * Respects upstream "Keep-Alive: timeout=X" headers.
     * Falls back to a safe 20-second default if the upstream server does not specify one.
     */
    @Bean
    public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        return (response, context) -> {
            Iterator<HeaderElement> it = MessageSupport.iterate(response, "Keep-Alive");
            while (it.hasNext()) {
                HeaderElement he = it.next();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && "timeout".equalsIgnoreCase(param)) {
                    try {
                        long timeoutSeconds = Long.parseLong(value);
                        return TimeValue.ofSeconds(timeoutSeconds);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid Keep-Alive timeout header value: {}", value);
                    }
                }
            }
            // Upstream sent no timeout header: default to 20 seconds
            return TimeValue.ofSeconds(20);
        };
    }

    @Bean
    public RestClient paymentGatewayRestClient(
            PoolingHttpClientConnectionManager connectionManager,
            ConnectionKeepAliveStrategy keepAliveStrategy) {

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(gatewayProperties.getTimeouts().getConnectionRequestTimeoutMs(), TimeUnit.MILLISECONDS))
                .setResponseTimeout(Timeout.of(gatewayProperties.getTimeouts().getSocketTimeoutMs(), TimeUnit.MILLISECONDS))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setKeepAliveStrategy(keepAliveStrategy) // Wire the strategy
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                // Proactively close idle sockets before upstream firewalls/ALBs kill them
                .evictIdleConnections(TimeValue.ofSeconds(gatewayProperties.getPool().getIdleEvictionSeconds()))
                .build();

        return RestClient.builder()
                .baseUrl(gatewayProperties.getBaseUrl())
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }


  /*  @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(gatewayProperties.getTimeouts().getConnectTimeoutMs(), TimeUnit.MILLISECONDS))
                .setSocketTimeout(Timeout.of(gatewayProperties.getTimeouts().getSocketTimeoutMs(), TimeUnit.MILLISECONDS))
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultConnectionConfig(connectionConfig);
        connectionManager.setMaxTotal(gatewayProperties.getPool().getMaxTotal());
        connectionManager.setDefaultMaxPerRoute(gatewayProperties.getPool().getDefaultMaxPerRoute());
        return connectionManager;
    }

    @Bean
    public RestClient paymentGatewayRestClient(PoolingHttpClientConnectionManager connectionManager) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(gatewayProperties.getTimeouts().getConnectionRequestTimeoutMs(), TimeUnit.MILLISECONDS))
                .setResponseTimeout(Timeout.of(gatewayProperties.getTimeouts().getSocketTimeoutMs(), TimeUnit.MILLISECONDS))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(gatewayProperties.getPool().getIdleEvictionSeconds()))
                .build();

        return RestClient.builder()
                .baseUrl(gatewayProperties.getBaseUrl())
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }*/
}