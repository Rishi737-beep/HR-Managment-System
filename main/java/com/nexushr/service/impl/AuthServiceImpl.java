package com.nexushr.service.impl;

import com.nexushr.dto.AuthDto;
import com.nexushr.entity.Department;
import com.nexushr.entity.Employee;
import com.nexushr.entity.EmployeeStatus;
import com.nexushr.entity.Role;
import com.nexushr.exception.BadRequestException;
import com.nexushr.exception.ResourceNotFoundException;
import com.nexushr.exception.UnauthorizedException;
import com.nexushr.repository.DepartmentRepository;
import com.nexushr.repository.EmployeeRepository;
import com.nexushr.security.EmployeePrincipal;
import com.nexushr.security.JwtTokenProvider;
import com.nexushr.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Value("${nexushr.jwt.refresh-expiration}")
    private long refreshExpirationMs;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    @Transactional
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        EmployeePrincipal principal = (EmployeePrincipal) authentication.getPrincipal();
        Employee employee = principal.getEmployee();

        String accessToken = tokenProvider.generateAccessToken(principal);
        String refreshToken = tokenProvider.generateRefreshToken();

        // Update refresh token in DB
        employee.setRefreshToken(refreshToken);
        employee.setRefreshTokenExpiry(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000));
        employeeRepository.save(employee);

        return AuthDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .id(employee.getId())
                .email(employee.getEmail())
                .role(employee.getRole().name())
                .employeeCode(employee.getEmployeeCode())
                .build();
    }

    @Override
    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use");
        }
        if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new BadRequestException("Employee Code already exists");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + request.getManagerId()));
        }

        Role assignedRole = request.getRole() != null ? request.getRole() : Role.EMPLOYEE;

        Employee employee = Employee.builder()
                .employeeCode(request.getEmployeeCode())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .designation(request.getDesignation())
                .department(department)
                .joiningDate(request.getJoiningDate())
                .salary(request.getSalary())
                .status(EmployeeStatus.ACTIVE)
                .manager(manager)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .build();

        Employee saved = employeeRepository.save(employee);

        // Auto login after registration to return tokens
        EmployeePrincipal principal = new EmployeePrincipal(saved);
        String accessToken = tokenProvider.generateAccessToken(principal);
        String refreshToken = tokenProvider.generateRefreshToken();

        saved.setRefreshToken(refreshToken);
        saved.setRefreshTokenExpiry(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000));
        employeeRepository.save(saved);

        return AuthDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .id(saved.getId())
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .employeeCode(saved.getEmployeeCode())
                .build();
    }

    @Override
    @Transactional
    public AuthDto.AuthResponse refresh(AuthDto.RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        Employee employee = employeeRepository.findByRefreshToken(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (employee.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            employee.setRefreshToken(null);
            employee.setRefreshTokenExpiry(null);
            employeeRepository.save(employee);
            throw new UnauthorizedException("Expired refresh token. Please login again.");
        }

        EmployeePrincipal principal = new EmployeePrincipal(employee);
        String newAccessToken = tokenProvider.generateAccessToken(principal);
        String newRefreshToken = tokenProvider.generateRefreshToken();

        employee.setRefreshToken(newRefreshToken);
        employee.setRefreshTokenExpiry(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000));
        employeeRepository.save(employee);

        return AuthDto.AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .id(employee.getId())
                .email(employee.getEmail())
                .role(employee.getRole().name())
                .employeeCode(employee.getEmployeeCode())
                .build();
    }

    @Override
    @Transactional
    public void logout(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employee.setRefreshToken(null);
        employee.setRefreshTokenExpiry(null);
        employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public void changePassword(String email, AuthDto.ChangePasswordRequest request) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), employee.getPasswordHash())) {
            throw new BadRequestException("Current password does not match");
        }

        employee.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        employeeRepository.save(employee);
    }

    @Override
    public void forgotPassword(AuthDto.ForgotPasswordRequest request) {
        // Find employee to check existence
        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + request.getEmail()));

        log.info("Simulated sending password reset email to: {}", employee.getEmail());
    }
}
