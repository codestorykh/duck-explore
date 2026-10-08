package com.duck.explore.retry;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RetryTimingSummary {
    private String operationName;
    private int totalAttempts;
    private List<Double> callDurationsSeconds;     // Execution time for each attempt
    private List<Double> backoffWaitSeconds;       // Wait time before each retry
    private double totalExecutionWaitSeconds;      // Total time spent calling the remote service
    private double totalBackoffWaitSeconds;        // Total time spent sleeping in backoff
    private double totalElapsedSeconds;            // Overall total time before exhaustion
}