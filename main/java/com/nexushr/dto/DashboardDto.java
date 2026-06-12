package com.nexushr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public final class DashboardDto {

    private DashboardDto() {}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminDashboard {
        private Long totalEmployees;
        private Long activeEmployees;
        private Long presentToday;
        private Long employeesOnLeave;
        private BigDecimal monthlyPayrollCost;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ManagerDashboard {
        private Long teamSize;
        private Double attendanceRate; // percentage
        private Long pendingLeaveRequests;
    }
}
