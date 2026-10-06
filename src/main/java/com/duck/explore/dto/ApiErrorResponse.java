package com.duck.explore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "title",
        "status",
        "detail",
        "instance",
        "timestamp",
        "invalidParams"
})
public class ApiErrorResponse {

    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;
    private Instant timestamp;
    private List<FieldErrorDetail> invalidParams;

    @Getter
    @Builder
    @JsonPropertyOrder({
            "code",
            "field",
            "rejectedValue",
            "reason"
    })
    public static class FieldErrorDetail {
        private String code;
        private String field;
        private Object rejectedValue;
        private String reason;
    }
}