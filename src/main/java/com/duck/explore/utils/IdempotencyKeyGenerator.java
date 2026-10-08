package com.duck.explore.utils;

import com.duck.explore.dto.GatewayDepositRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class IdempotencyKeyGenerator {

    /**
     * Generates a unique, non-colliding random key (standard UUIDv4).
     * Format: dep_3fa85f64-5717-4562-b3fc-2c963f66afa6
     */
    public String generateRandomKey(String prefix) {
        String cleanPrefix = (prefix == null || prefix.isBlank()) ? "req" : prefix;
        return cleanPrefix + "_" + UUID.randomUUID();
    }

    /**
     * Generates a deterministic idempotency key by hashing the transaction parameters.
     * Prevents accidental double-clicks within the same time window (e.g., 5-minute slice).
     *
     * Result format: dep_hash_7f83b1657ff1...
     */
    public String generatePayloadHashKey(GatewayDepositRequest request, String clientReference) {
        try {
            // Bucket into 5-minute slices to prevent infinite lockouts if an intention expires
            long timeBucket = Instant.now().truncatedTo(ChronoUnit.MINUTES).getEpochSecond() / 300;

            String rawSignature = String.join(":",
                    clientReference != null ? clientReference : "N/A",
                    request.getAccountNo(),
                    request.getAmount() != null ? request.getAmount().toPlainString() : "0",
                    request.getCurrency(),
                    String.valueOf(timeBucket)
            );

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(rawSignature.getBytes(StandardCharsets.UTF_8));
            
            return "dep_hash_" + HexFormat.of().formatHex(encodedHash).substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            // Fallback in the rare event SHA-256 is missing
            return generateRandomKey("dep_fallback");
        }
    }
}