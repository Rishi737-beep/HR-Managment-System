package com.nexushr.dto;

import com.nexushr.entity.PaymentStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class PayrollDto {

    private PayrollDto() {}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenerateRequest {
        @NotNull(message = "Employee ID is required")
        private Long employeeId;

        @NotNull(message = "Month is required")
        @Min(value = 1, message = "Month must be between 1 and 12")
        @Max(value = 12, message = "Month must be between 1 and 12")
        private Integer month;

        @NotNull(message = "Year is required")
        private Integer year;

        private BigDecimal basicSalary;
        private BigDecimal allowances;
        private BigDecimal deductions;
        private PaymentStatus paymentStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private String designation;
        private String departmentName;
        private BigDecimal basicSalary;
        private BigDecimal allowances;
        private BigDecimal deductions;
        private BigDecimal netSalary;
        private Integer month;
        private Integer year;
        private PaymentStatus paymentStatus;
        private LocalDateTime createdAt;
    }
}
