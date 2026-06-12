package com.nexushr.dto;

import com.nexushr.entity.LeaveStatus;
import com.nexushr.entity.LeaveType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class LeaveDto {

    private LeaveDto() {}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApplyRequest {
        @NotNull(message = "Leave type is required")
        private LeaveType leaveType;

        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date must be in the present or future")
        private LocalDate startDate;

        @NotNull(message = "End date is required")
        private LocalDate endDate;

        @NotBlank(message = "Reason is required")
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private LeaveType leaveType;
        private LocalDate startDate;
        private LocalDate endDate;
        private String reason;
        private LeaveStatus status;
        private Long approvedById;
        private String approvedByName;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BalanceResponse {
        private LeaveType leaveType;
        private Integer totalEntitled;
        private Integer used;
        private Integer remaining;
    }
}
