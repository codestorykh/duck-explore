package com.duck.explore.config;

import com.duck.explore.config.properties.ExternalGatewayProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;

@Slf4j
@Configuration
public class CustomRetryConfig {

    @Bean
    public RetryTemplate paymentGatewayRetryTemplate(ExternalGatewayProperties props) {
        RetryTemplate template = new RetryTemplate();

        // 1. Backoff configuration
        ExponentialRandomBackOffPolicy backoffPolicy = new ExponentialRandomBackOffPolicy();
        backoffPolicy.setInitialInterval(props.getRetry().getDelay());
        backoffPolicy.setMultiplier(props.getRetry().getMultiplier());
        template.setBackOffPolicy(backoffPolicy);

        // 2. Exception Classification Policy
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(props.getRetry().getMaxAttempts());
        NeverRetryPolicy neverRetryPolicy = new NeverRetryPolicy();

        ExceptionClassifierRetryPolicy classifierPolicy = new ExceptionClassifierRetryPolicy();
        classifierPolicy.setExceptionClassifier(throwable -> {
            // Check for Read Timeout (SocketTimeoutException that is NOT ConnectTimeout)
            if (isSocketReadTimeout(throwable)) {
                log.info("Socket Read Timeout");
                return neverRetryPolicy; // Do not retry read timeouts
            }

            if (throwable instanceof HttpServerErrorException) {
                log.info("Http Server Error");
                return retryPolicy; // Retry 502/503/504
            }

            if (throwable instanceof ResourceAccessException) {
                log.info("Resource Access Exception");
                // Connection failures, connect timeouts, host down: safe to retry
                return retryPolicy;
            }

            return neverRetryPolicy;
        });

        template.setRetryPolicy(classifierPolicy);
        return template;
    }

    private boolean isSocketReadTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException && !(current instanceof ConnectTimeoutException)) {
                if (current.getMessage() == null || !current.getMessage().toLowerCase().contains("connect")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}