package com.duck.explore.exception;

import com.duck.explore.dto.ApiErrorResponse;
import com.duck.explore.dto.ApiErrorResponse.FieldErrorDetail;
import com.duck.explore.validation.error.ValidationErrorCode;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.Payload;
import jakarta.validation.metadata.ConstraintDescriptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    private static final Map<Class<?>, String> PAYLOAD_CODE_MAP = Map.of(
            ValidationErrorCode.AccountBlank.class, "ERR_VAL_001",
            ValidationErrorCode.AccountFormat.class, "ERR_VAL_002",
            ValidationErrorCode.AmountNull.class, "ERR_VAL_003",
            ValidationErrorCode.AmountMin.class, "ERR_VAL_004",
            ValidationErrorCode.CurrencyInvalid.class, "ERR_VAL_005"
    );

    private static final Map<String, String> CONSTRAINT_NAME_FALLBACK_MAP = Map.of(
            "NotBlank", "ERR_VAL_REQUIRED_STRING",
            "NotNull", "ERR_VAL_REQUIRED_FIELD",
            "DecimalMin", "ERR_VAL_BELOW_MIN",
            "Digits", "ERR_VAL_INVALID_DIGITS",
            "Pattern", "ERR_VAL_PATTERN_MISMATCH"
    );

    /**
     * Catches HTTP @Valid @RequestBody failures from Controllers.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Locale currentLocale = LocaleContextHolder.getLocale();

        List<FieldErrorDetail> errorDetails = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> {
                    String code = extractConstraintCodeFromFieldError(fieldError);
                    String localizedReason = messageSource.getMessage(fieldError, currentLocale);

                    return FieldErrorDetail.builder()
                            .code(code)
                            .field(fieldError.getField())
                            .rejectedValue(fieldError.getRejectedValue())
                            .reason(localizedReason)
                            .build();
                })
                .toList();

        ApiErrorResponse response = ApiErrorResponse.builder()
                .type("https://localhost:8080/errors/validation-failed")
                .title(messageSource.getMessage("validation.title", null, "Validation Failed", currentLocale))
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(String.format("Found %d validation error(s)", errorDetails.size()))
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .invalidParams(errorDetails)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Catches ConstraintViolationException (e.g. from manual validator.validate()
     * or Spring @Validated on @RequestParam / @PathVariable).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        Locale currentLocale = LocaleContextHolder.getLocale();

        List<FieldErrorDetail> errorDetails = ex.getConstraintViolations()
                .stream()
                .map(violation -> FieldErrorDetail.builder()
                        .code(extractConstraintCode(violation.getConstraintDescriptor()))
                        .field(extractLeafFieldName(violation.getPropertyPath()))
                        .rejectedValue(violation.getInvalidValue())
                        .reason(violation.getMessage())
                        .build())
                .toList();

        ApiErrorResponse response = ApiErrorResponse.builder()
                .type("https://localhost:8080/errors/validation-failed")
                .title(messageSource.getMessage("validation.title", null, "Validation Failed", currentLocale))
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(String.format("Found %d validation error(s)", errorDetails.size()))
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .invalidParams(errorDetails)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        Locale currentLocale = LocaleContextHolder.getLocale();
        Throwable cause = ex.getCause();

        List<FieldErrorDetail> errorDetails;

        if (cause instanceof InvalidFormatException ife) {
            String fieldName = "unknown";
            if (ife.getPath() != null && !ife.getPath().isEmpty()) {
                var lastRef = ife.getPath().getLast();
                fieldName = lastRef.getPropertyName(); // <-- In Jackson 3, it's getPropertyName()
            }

            String targetType = ife.getTargetType() != null
                    ? ife.getTargetType().getSimpleName()
                    : "valid format";

            String reason = messageSource.getMessage(
                    "validation.type.mismatch",
                    new Object[]{targetType},
                    String.format("Invalid format for value '%s'. Expected %s.", ife.getValue(), targetType),
                    currentLocale
            );

            errorDetails = List.of(
                    FieldErrorDetail.builder()
                            .code("ERR_VAL_TYPE_MISMATCH")
                            .field(fieldName)
                            .rejectedValue(ife.getValue())
                            .reason(reason)
                            .build()
            );
        } else {
            // General malformed JSON (e.g. trailing commas, unclosed brackets)
            errorDetails = List.of(
                    FieldErrorDetail.builder()
                            .code("ERR_MALFORMED_JSON")
                            .field("requestBody")
                            .rejectedValue(null)
                            .reason(messageSource.getMessage("validation.malformed.json", null, "Malformed JSON request body", currentLocale))
                            .build()
            );
        }

        ApiErrorResponse response = ApiErrorResponse.builder()
                .type("https://localhost:8080/errors/bad-request")
                .title(messageSource.getMessage("validation.title", null, "Validation Failed", currentLocale))
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Request payload could not be parsed or deserialized")
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .invalidParams(errorDetails)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private String extractConstraintCodeFromFieldError(FieldError fieldError) {
        try {
            ConstraintDescriptor<?> descriptor = fieldError.unwrap(ConstraintDescriptor.class);
            return extractConstraintCode(descriptor);
        } catch (Exception ex) {
            return "ERR_VAL_GENERIC";
        }
    }

    private String extractLeafFieldName(Path propertyPath) {
        String leafName = "";
        for (Path.Node node : propertyPath) {
            leafName = node.getName();
        }
        return leafName;
    }

    private String extractConstraintCode(ConstraintDescriptor<?> descriptor) {
        if (descriptor == null) {
            return "ERR_VAL_GENERIC";
        }

        Set<Class<? extends Payload>> payloadClasses = descriptor.getPayload();
        for (Class<?> payloadClass : payloadClasses) {
            if (PAYLOAD_CODE_MAP.containsKey(payloadClass)) {
                return PAYLOAD_CODE_MAP.get(payloadClass);
            }
        }

        String annotationName = descriptor.getAnnotation().annotationType().getSimpleName();
        return CONSTRAINT_NAME_FALLBACK_MAP.getOrDefault(annotationName, "ERR_VAL_GENERIC");
    }
}