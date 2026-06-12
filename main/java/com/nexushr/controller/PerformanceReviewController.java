package com.nexushr.controller;

import com.nexushr.dto.PerformanceDto;
import com.nexushr.security.EmployeePrincipal;
import com.nexushr.service.PerformanceReviewService;
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
@RequestMapping("/api/performance-reviews")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Performance Management", description = "Endpoints for creating, updating, and reading employee performance reviews")
public class PerformanceReviewController {

    private final PerformanceReviewService performanceReviewService;

    public PerformanceReviewController(PerformanceReviewService performanceReviewService) {
        this.performanceReviewService = performanceReviewService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Submit a new performance review", description = "Privileged users only. Reviewer ID is mapped from authenticated session principal.")
    public ResponseEntity<PerformanceDto.Response> addReview(
            @AuthenticationPrincipal EmployeePrincipal principal,
            @Valid @RequestBody PerformanceDto.Request request
    ) {
        return new ResponseEntity<>(performanceReviewService.addReview(principal.getId(), request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Update an existing performance review", description = "Only the reviewer who created it is allowed to modify details")
    public ResponseEntity<PerformanceDto.Response> updateReview(
            @AuthenticationPrincipal EmployeePrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody PerformanceDto.Request request
    ) {
        return ResponseEntity.ok(performanceReviewService.updateReview(principal.getId(), id, request));
    }

    @GetMapping("/history/{employeeId}")
    @Operation(summary = "Get performance review history for an employee", description = "Admin/HR/Manager can query anyone. Regular employees can only query their own history.")
    public ResponseEntity<List<PerformanceDto.Response>> getReviewHistory(
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

        return ResponseEntity.ok(performanceReviewService.getReviewHistory(targetEmployeeId));
    }
}
