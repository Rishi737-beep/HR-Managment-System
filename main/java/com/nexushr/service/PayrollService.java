package com.nexushr.service;

import com.nexushr.dto.PayrollDto;

import java.util.List;

public interface PayrollService {
    PayrollDto.Response generate(PayrollDto.GenerateRequest request);
    PayrollDto.Response getPayslip(Long id);
    List<PayrollDto.Response> getHistory(Long employeeId);
}
