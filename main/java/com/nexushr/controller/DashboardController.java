package com.nexushr.controller;

import com.nexushr.dto.DashboardDto;
import com.nexushr.security.EmployeePrincipal;
import com.nexushr.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard Analytics", description = "Endpoints for fetching high-level analytics for admins and managers")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get admin dashboard metrics", description = "Admin or HR privilege required. Returns total/active counts, present count today, leaves, and monthly costs.")
    public ResponseEntity<DashboardDto.AdminDashboard> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get manager dashboard metrics", description = "Managers can only query their own team metrics. Admin/HR can query anyone.")
    public ResponseEntity<DashboardDto.ManagerDashboard> getManagerDashboard(
            @AuthenticationPrincipal EmployeePrincipal principal,
            @PathVariable Long managerId
    ) {
        Long targetManagerId = managerId;
        boolean isPrivileged = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_HR"));

        if (!isPrivileged) {
            targetManagerId = principal.getId();
        }

        return ResponseEntity.ok(dashboardService.getManagerDashboard(targetManagerId));
    }
}
