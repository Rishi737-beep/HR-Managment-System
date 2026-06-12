package com.nexushr.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class PerformanceDto {

    private PerformanceDto() {}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotNull(message = "Employee ID is required")
        private Long employeeId;

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        private Integer rating;

        @NotBlank(message = "Feedback is required")
        private String feedback;

        @NotNull(message = "Review date is required")
        @PastOrPresent(message = "Review date cannot be in the future")
        private LocalDate reviewDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private Long reviewerId;
        private String reviewerName;
        private Integer rating;
        private String feedback;
        private LocalDate reviewDate;
        private LocalDateTime createdAt;
    }
}
