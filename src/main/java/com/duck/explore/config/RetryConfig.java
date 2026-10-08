package com.duck.explore.config;

import com.duck.explore.retry.TimingRetryListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class RetryConfig {

    @Bean
    public TimingRetryListener timingRetryListener() {
        return new TimingRetryListener();
    }
}