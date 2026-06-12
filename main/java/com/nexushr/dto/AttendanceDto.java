package com.nexushr.dto;

import com.nexushr.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class AttendanceDto {

    private AttendanceDto() {}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private LocalDate date;
        private LocalDateTime checkInTime;
        private LocalDateTime checkOutTime;
        private BigDecimal workingHours;
        private AttendanceStatus status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SummaryResponse {
        private Integer month;
        private Integer year;
        private Long totalDaysInMonth;
        private Long presentDays;
        private Long absentDays;
        private Long lateDays;
        private Long halfDays;
        private Double attendanceRate;
    }
}
