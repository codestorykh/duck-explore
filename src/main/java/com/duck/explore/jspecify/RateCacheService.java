package com.duck.explore.usecase;

import java.util.Map;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;

@NullMarked
@Service
public class RateCacheService {

    private final Map<String, Double> fxRateMap = Map.of("USD", 1.0, "KHR", 4100.0);

    public double getRate(String currency) {

        // COMPILE ERROR: unboxing @Nullable Double to primitive double can cause NPE
        //return fxRateMap.get(currency);

        // Safe: provides a non-null primitive fallback
        return fxRateMap.getOrDefault(currency, 0.0);
    }

    public Double getRateStrict(String currency) {
        Double rate = fxRateMap.get(currency);
        if (rate == null) {
            throw new IllegalArgumentException("Exchange rate not configured for currency: " + currency);
        }
        return rate; // NullAway knows rate cannot be null after the guard
    }
}