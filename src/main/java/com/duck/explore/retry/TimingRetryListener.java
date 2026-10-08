package com.duck.explore.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TimingRetryListener implements RetryListener {

    private static final String START_TIME = "timing.overall_start";
    private static final String PREVIOUS_TIMESTAMP = "timing.previous_timestamp";
    private static final String CALL_DURATIONS = "timing.call_durations";
    private static final String BACKOFF_DURATIONS = "timing.backoff_durations";

    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        long now = System.currentTimeMillis();
        context.setAttribute(START_TIME, now);
        context.setAttribute(PREVIOUS_TIMESTAMP, now);
        context.setAttribute(CALL_DURATIONS, new ArrayList<Long>());
        context.setAttribute(BACKOFF_DURATIONS, new ArrayList<Long>());
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        long now = System.currentTimeMillis();

        Long previousTimestamp = (Long) context.getAttribute(PREVIOUS_TIMESTAMP);
        List<Long> callDurations = (List<Long>) context.getAttribute(CALL_DURATIONS);
        List<Long> backoffDurations = (List<Long>) context.getAttribute(BACKOFF_DURATIONS);

        if (previousTimestamp != null && callDurations != null) {
            // If this is attempt 2 or 3, calculate the backoff sleep that occurred BEFORE this call
            int retryCount = context.getRetryCount(); // 1 on first failure, 2 on second failure, etc.

            if (retryCount > 1 && backoffDurations != null) {
                // The time between the last failure and now includes (backoff + current attempt execution)
                // We record cleanly by keeping PREVIOUS_TIMESTAMP updated
            }

            long currentCallDuration = now - previousTimestamp;
            callDurations.add(currentCallDuration);
        }

        // Set previous timestamp to NOW (where backoff begins)
        context.setAttribute(PREVIOUS_TIMESTAMP, now);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        long now = System.currentTimeMillis();
        Long totalStart = (Long) context.getAttribute(START_TIME);
        long totalElapsedMs = (totalStart != null) ? (now - totalStart) : 0L;

        List<Long> callDurations = (List<Long>) context.getAttribute(CALL_DURATIONS);
        if (callDurations == null) callDurations = List.of();

        long sumCalls = callDurations.stream().mapToLong(Long::longValue).sum();
        long sumBackoffs = Math.max(0, totalElapsedMs - sumCalls);

        StringBuilder sb = new StringBuilder();
        sb.append("\n================ RETRY TIMING SUMMARY ================\n");
        sb.append(String.format("Status: %s (Total Attempts: %d)\n", (throwable != null) ? "EXHAUSTED / FAILED" : "SUCCEEDED", context.getRetryCount()));

        for (int i = 0; i < callDurations.size(); i++) {
            sb.append(String.format(" - Attempt #%d execution/connection wait: %.3f s\n", (i + 1), callDurations.get(i) / 1000.0));
        }

        sb.append("------------------------------------------------------\n");
        sb.append(String.format("Sum of Connection/Call Waits: %.3f s\n", sumCalls / 1000.0));
        sb.append(String.format("Sum of Backoff Waits:         %.3f s\n", sumBackoffs / 1000.0));
        sb.append(String.format("TOTAL ELAPSED TIME:           %.3f s\n", totalElapsedMs / 1000.0));
        sb.append("======================================================\n");

        log.info(sb.toString());
    }
}