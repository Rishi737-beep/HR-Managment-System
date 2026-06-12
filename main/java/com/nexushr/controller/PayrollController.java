package com.nexushr.controller;

import com.nexushr.dto.PayrollDto;
import com.nexushr.security.EmployeePrincipal;
import com.nexushr.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Payroll Management", description = "Endpoints for generating payroll, retrieving payslips, and viewing history")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Generate monthly payroll for an employee", description = "Admin or HR privilege required. Computes net salary using Basic + Allowances - Deductions.")
    public ResponseEntity<PayrollDto.Response> generatePayroll(@Valid @RequestBody PayrollDto.GenerateRequest request) {
        return new ResponseEntity<>(payrollService.generate(request), HttpStatus.CREATED);
    }

    @GetMapping("/payslip/{id}")
    @Operation(summary = "Retrieve a payslip by ID", description = "Regular employees can only view their own payslips")
    public ResponseEntity<PayrollDto.Response> getPayslip(
            @AuthenticationPrincipal EmployeePrincipal principal,
            @PathVariable Long id
    ) {
        PayrollDto.Response response = payrollService.getPayslip(id);
        
        boolean isPrivileged = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_HR"));

        if (!isPrivileged && !response.getEmployeeId().equals(principal.getId())) {
            throw new AccessDeniedException("You do not have permission to access this payslip");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(summary = "Get payroll history", description = "Admin/HR can query anyone. Regular employees can only query their own history.")
    public ResponseEntity<List<PayrollDto.Response>> getHistory(
            @AuthenticationPrincipal EmployeePrincipal principal,
            @RequestParam(required = false) Long employeeId
    ) {
        Long targetEmployeeId = employeeId;
        boolean isPrivileged = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_HR"));

        if (!isPrivileged || targetEmployeeId == null) {
            targetEmployeeId = principal.getId();
        }

        return ResponseEntity.ok(payrollService.getHistory(targetEmployeeId));
    }
}
