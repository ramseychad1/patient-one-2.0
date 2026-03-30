package com.hubaccess.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private T data;
    private Meta meta;
    private ErrorBody error;

    @Data
    @Builder
    public static class Meta {
        private String timestamp;
        private String requestId;
    }

    @Data
    @Builder
    public static class ErrorBody {
        private String code;
        private String message;
        private String field;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .meta(Meta.builder()
                        .timestamp(OffsetDateTime.now().toString())
                        .requestId(UUID.randomUUID().toString())
                        .build())
                .build();
    }

    public static ApiResponse<?> error(String code, String message) {
        return ApiResponse.builder()
                .error(ErrorBody.builder().code(code).message(message).build())
                .meta(Meta.builder()
                        .timestamp(OffsetDateTime.now().toString())
                        .requestId(UUID.randomUUID().toString())
                        .build())
                .build();
    }

    public static ApiResponse<?> error(String code, String message, String field) {
        return ApiResponse.builder()
                .error(ErrorBody.builder().code(code).message(message).field(field).build())
                .meta(Meta.builder()
                        .timestamp(OffsetDateTime.now().toString())
                        .requestId(UUID.randomUUID().toString())
                        .build())
                .build();
    }
}
