package com.nexushr.dto;

import com.nexushr.entity.NotificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public final class NotificationDto {

    private NotificationDto() {}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Message is required")
        private String message;

        @NotNull(message = "Recipient ID is required")
        private Long recipientId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String message;
        private Long recipientId;
        private NotificationStatus status;
        private LocalDateTime createdAt;
    }
}
