package com.duck.explore.retry;

import org.springframework.stereotype.Component;

@Component
public class RetryDurationCalculator {

    /**
     * Calculates the theoretical maximum seconds a request thread will block
     * when all retries are exhausted under timeout conditions.
     */
    public static double calculateMaxWaitSeconds(
            int maxAttempts,
            double connectionTimeoutSeconds,
            double readTimeoutSeconds,
            double initialBackoffSeconds,
            double multiplier) {

        double singleCallTimeout = connectionTimeoutSeconds + readTimeoutSeconds;
        double totalCallDuration = singleCallTimeout * maxAttempts;

        double totalBackoff = 0.0;
        double currentBackoff = initialBackoffSeconds;

        // Number of backoffs is (maxAttempts - 1)
        for (int i = 1; i < maxAttempts; i++) {
            totalBackoff += currentBackoff;
            currentBackoff *= multiplier;
        }

        return totalCallDuration + totalBackoff;
    }
}