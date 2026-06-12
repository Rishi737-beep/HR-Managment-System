package com.nexushr.service;

import com.nexushr.dto.DashboardDto;

public interface DashboardService {
    DashboardDto.AdminDashboard getAdminDashboard();
    DashboardDto.ManagerDashboard getManagerDashboard(Long managerId);
}
