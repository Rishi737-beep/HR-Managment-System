package com.nexushr.controller;

import com.nexushr.dto.EmployeeDto;
import com.nexushr.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Employee Management", description = "Endpoints for employee CRUD, search, pagination, and sorting")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create a new employee profile", description = "Admin or HR privilege required. Creates employee profile details.")
    public ResponseEntity<EmployeeDto.Response> createEmployee(@Valid @RequestBody EmployeeDto.CreateRequest request) {
        return new ResponseEntity<>(employeeService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update employee details by ID", description = "Admin or HR privilege required. Updates personal and professional details.")
    public ResponseEntity<EmployeeDto.Response> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeDto.UpdateRequest request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee profile by ID", description = "Accessible by any authenticated employee")
    public ResponseEntity<EmployeeDto.Response> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get paginated, sorted list of employees", description = "Supports custom pagination page index, page size, sort criteria, and sorting direction.")
    public ResponseEntity<Page<EmployeeDto.Response>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(employeeService.getAll(page, size, sortBy, sortDir));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Search employees with keyword", description = "Searches for matching text in first name, last name, code, and designation fields")
    public ResponseEntity<Page<EmployeeDto.Response>> searchEmployees(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(employeeService.search(query, page, size, sortBy, sortDir));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an employee profile", description = "Admin privilege required. Permanently deletes employee metadata.")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
