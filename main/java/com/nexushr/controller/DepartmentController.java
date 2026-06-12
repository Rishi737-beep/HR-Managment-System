package com.nexushr.controller;

import com.nexushr.dto.DepartmentDto;
import com.nexushr.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Department Management", description = "Endpoints for department creation, retrieval, updates, and deletion")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create a new department", description = "Admin or HR privilege required")
    public ResponseEntity<DepartmentDto.Response> createDepartment(@Valid @RequestBody DepartmentDto.Request request) {
        return new ResponseEntity<>(departmentService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update department details by ID", description = "Admin or HR privilege required")
    public ResponseEntity<DepartmentDto.Response> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentDto.Request request) {
        return ResponseEntity.ok(departmentService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department details by ID", description = "Accessible by any authenticated employee")
    public ResponseEntity<DepartmentDto.Response> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get list of all departments", description = "Accessible by any authenticated employee")
    public ResponseEntity<List<DepartmentDto.Response>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAll());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete department by ID", description = "Admin privilege required")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
