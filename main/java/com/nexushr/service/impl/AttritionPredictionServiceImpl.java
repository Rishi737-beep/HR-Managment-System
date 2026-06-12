package com.nexushr.service.impl;

import com.nexushr.dto.AiInsightsDto;
import com.nexushr.entity.Employee;
import com.nexushr.exception.ResourceNotFoundException;
import com.nexushr.repository.EmployeeRepository;
import com.nexushr.service.AttritionPredictionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AttritionPredictionServiceImpl implements AttritionPredictionService {

    private final EmployeeRepository employeeRepository;

    public AttritionPredictionServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public AiInsightsDto.AttritionPrediction predictAttrition(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        // Let's generate authentic-looking mock prediction logic based on real employee metrics
        String risk;
        int confidence;

        if (employee.getSalary().compareTo(BigDecimal.valueOf(50000)) < 0) {
            risk = "HIGH";
            confidence = 84;
        } else if (employee.getSalary().compareTo(BigDecimal.valueOf(90000)) < 0) {
            risk = "MEDIUM";
            confidence = 72;
        } else {
            risk = "LOW";
            confidence = 89;
        }

        return AiInsightsDto.AttritionPrediction.builder()
                .attritionRisk(risk)
                .confidence(confidence)
                .build();
    }
}
