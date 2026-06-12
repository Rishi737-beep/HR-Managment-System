package com.nexushr.dto;

import com.nexushr.entity.EmployeeStatus;
import com.nexushr.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class EmployeeDto {

    private EmployeeDto() {}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String employeeCode;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String designation;
        private Long departmentId;
        private String departmentName;
        private LocalDate joiningDate;
        private BigDecimal salary;
        private EmployeeStatus status;
        private Long managerId;
        private String managerName;
        private String profilePhotoUrl;
        private Role role;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Employee code is required")
        private String employeeCode;

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        private String phone;

        @NotBlank(message = "Designation is required")
        private String designation;

        @NotNull(message = "Department ID is required")
        private Long departmentId;

        @NotNull(message = "Joining date is required")
        private LocalDate joiningDate;

        @NotNull(message = "Salary is required")
        private BigDecimal salary;

        @NotBlank(message = "Password is required")
        private String password;

        private Role role;

        private Long managerId;

        private String profilePhotoUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        private String phone;

        @NotBlank(message = "Designation is required")
        private String designation;

        @NotNull(message = "Department ID is required")
        private Long departmentId;

        @NotNull(message = "Salary is required")
        private BigDecimal salary;

        @NotNull(message = "Status is required")
        private EmployeeStatus status;

        private Long managerId;

        private String profilePhotoUrl;

        @NotNull(message = "Role is required")
        private Role role;
    }
}
