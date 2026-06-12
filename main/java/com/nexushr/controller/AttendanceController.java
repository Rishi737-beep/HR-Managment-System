package com.nexushr.controller;

import com.nexushr.dto.AttendanceDto;
import com.nexushr.security.EmployeePrincipal;
import com.nexushr.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Attendance Management", description = "Endpoints for checking in, checking out, and fetching attendance logs/summaries")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/check-in")
    @Operation(summary = "Perform employee check-in", description = "Registers start timestamp for today and returns attendance log details")
    public ResponseEntity<AttendanceDto.Response> checkIn(@AuthenticationPrincipal EmployeePrincipal principal) {
        return ResponseEntity.ok(attendanceService.checkIn(principal.getId()));
    }

    @PostMapping("/check-out")
    @Operation(summary = "Perform employee check-out", description = "Registers end timestamp, computes duration, updates today's attendance status")
    public ResponseEntity<AttendanceDto.Response> checkOut(@AuthenticationPrincipal EmployeePrincipal principal) {
        return ResponseEntity.ok(attendanceService.checkOut(principal.getId()));
    }

    @GetMapping("/report")
    @Operation(summary = "Fetch attendance reports between date bounds", description = "Admin/HR/Manager can query anyone. Regular employees can only query themselves.")
    public ResponseEntity<List<AttendanceDto.Response>> getReport(
            @AuthenticationPrincipal EmployeePrincipal principal,
            @RequestParam(required = false) Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Long targetEmployeeId = employeeId;
        // Non-admin/HR/Manager cannot query other employee's attendance
        boolean isPrivileged = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_HR") ||
                        a.getAuthority().equals("ROLE_MANAGER"));

        if (!isPrivileged || targetEmployeeId == null) {
            targetEmployeeId = principal.getId();
        }

        return ResponseEntity.ok(attendanceService.getReport(targetEmployeeId, startDate, endDate));
    }

    @GetMapping("/summary/{employeeId}")
    @Operation(summary = "Fetch monthly attendance aggregates", description = "Returns active, late, half days, and attendance rate metrics")
    public ResponseEntity<AttendanceDto.SummaryResponse> getMonthlySummary(
            @AuthenticationPrincipal EmployeePrincipal principal,
            @PathVariable Long employeeId,
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        Long targetEmployeeId = employeeId;
        boolean isPrivileged = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_HR") ||
                        a.getAuthority().equals("ROLE_MANAGER"));

        if (!isPrivileged) {
            targetEmployeeId = principal.getId();
        }

        return ResponseEntity.ok(attendanceService.getMonthlySummary(targetEmployeeId, month, year));
    }
}
