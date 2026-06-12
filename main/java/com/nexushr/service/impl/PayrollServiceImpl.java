package com.nexushr.service.impl;

import com.nexushr.dto.PayrollDto;
import com.nexushr.entity.Employee;
import com.nexushr.entity.PaymentStatus;
import com.nexushr.entity.Payroll;
import com.nexushr.exception.BadRequestException;
import com.nexushr.exception.ResourceNotFoundException;
import com.nexushr.repository.EmployeeRepository;
import com.nexushr.repository.PayrollRepository;
import com.nexushr.service.PayrollService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;

    public PayrollServiceImpl(PayrollRepository payrollRepository, EmployeeRepository employeeRepository) {
        this.payrollRepository = payrollRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public PayrollDto.Response generate(PayrollDto.GenerateRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        if (payrollRepository.existsByEmployeeIdAndMonthAndYear(request.getEmployeeId(), request.getMonth(), request.getYear())) {
            throw new BadRequestException("Payroll already generated for employee: " + request.getEmployeeId() +
                    " in month: " + request.getMonth() + ", year: " + request.getYear());
        }

        BigDecimal basicSalary = request.getBasicSalary() != null ? request.getBasicSalary() : employee.getSalary();
        BigDecimal allowances = request.getAllowances() != null ? request.getAllowances() : BigDecimal.ZERO;
        BigDecimal deductions = request.getDeductions() != null ? request.getDeductions() : BigDecimal.ZERO;
        PaymentStatus status = request.getPaymentStatus() != null ? request.getPaymentStatus() : PaymentStatus.UNPAID;

        Payroll payroll = Payroll.builder()
                .employee(employee)
                .month(request.getMonth())
                .year(request.getYear())
                .basicSalary(basicSalary)
                .allowances(allowances)
                .deductions(deductions)
                .paymentStatus(status)
                .build();

        // Trigger calculations
        payroll.calculateNetSalary();

        Payroll saved = payrollRepository.save(payroll);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollDto.Response getPayslip(Long id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with ID: " + id));
        return mapToResponse(payroll);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollDto.Response> getHistory(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }
        return payrollRepository.findByEmployeeIdOrderByYearDescMonthDesc(employeeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PayrollDto.Response mapToResponse(Payroll payroll) {
        return PayrollDto.Response.builder()
                .id(payroll.getId())
                .employeeId(payroll.getEmployee().getId())
                .employeeName(payroll.getEmployee().getFirstName() + " " + payroll.getEmployee().getLastName())
                .designation(payroll.getEmployee().getDesignation())
                .departmentName(payroll.getEmployee().getDepartment() != null ? payroll.getEmployee().getDepartment().getName() : "N/A")
                .basicSalary(payroll.getBasicSalary())
                .allowances(payroll.getAllowances())
                .deductions(payroll.getDeductions())
                .netSalary(payroll.getNetSalary())
                .month(payroll.getMonth())
                .year(payroll.getYear())
                .paymentStatus(payroll.getPaymentStatus())
                .createdAt(payroll.getCreatedAt())
                .build();
    }
}
