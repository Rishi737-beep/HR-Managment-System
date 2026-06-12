package com.nexushr.controller;

import com.nexushr.dto.LeaveDto;
import com.nexushr.security.EmployeePrincipal;
import com.nexushr.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Leave Management", description = "Endpoints for applying, approving, rejecting, and viewing leave requests and balances")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    @Operation(summary = "Apply for leave", description = "Submits a pending leave request after checking entitlements balance")
    public ResponseEntity<LeaveDto.Response> applyLeave(
            @AuthenticationPrincipal EmployeePrincipal principal,
            @Valid @RequestBody LeaveDto.ApplyRequest request
    ) {
        return new ResponseEntity<>(leaveService.apply(principal.getId(), request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Approve a leave request", description = "Privileged users only. Changes status to APPROVED and sets approver metadata.")
    public ResponseEntity<LeaveDto.Response> approveLeave(
            @AuthenticationPrincipal EmployeePrincipal principal,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(leaveService.approve(id, principal.getId()));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Reject a leave request", description = "Privileged users only. Changes status to REJECTED and sets rejector metadata.")
    public ResponseEntity<LeaveDto.Response> rejectLeave(
            @AuthenticationPrincipal EmployeePrincipal principal,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(leaveService.reject(id, principal.getId()));
    }

    @GetMapping
    @Operation(summary = "Get list of leave requests", description = "Admin/HR gets all. Managers get team + own requests. Employees get own requests.")
    public ResponseEntity<List<LeaveDto.Response>> getLeaves(@AuthenticationPrincipal EmployeePrincipal principal) {
        String role = principal.getAuthorities().iterator().next().getAuthority();
        return ResponseEntity.ok(leaveService.getLeaves(principal.getId(), role));
    }

    @GetMapping("/balance/{employeeId}")
    @Operation(summary = "Get leave balance entitlements and usage", description = "Admin/HR/Manager can query anyone. Regular employees can only query themselves.")
    public ResponseEntity<List<LeaveDto.BalanceResponse>> getLeaveBalance(
            @AuthenticationPrincipal EmployeePrincipal principal,
            @PathVariable Long employeeId
    ) {
        Long targetEmployeeId = employeeId;
        boolean isPrivileged = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_HR") ||
                        a.getAuthority().equals("ROLE_MANAGER"));

        if (!isPrivileged) {
            targetEmployeeId = principal.getId();
        }

        return ResponseEntity.ok(leaveService.getLeaveBalance(targetEmployeeId));
    }
}
