package com.nexushr.service.impl;

import com.nexushr.dto.AiInsightsDto;
import com.nexushr.entity.Employee;
import com.nexushr.exception.ResourceNotFoundException;
import com.nexushr.repository.EmployeeRepository;
import com.nexushr.service.EmployeeEngagementService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class EmployeeEngagementServiceImpl implements EmployeeEngagementService {

    private final EmployeeRepository employeeRepository;

    public EmployeeEngagementServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public AiInsightsDto.EmployeeEngagement assessEngagement(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        int score;
        String sentiment;
        List<String> drivers;

        // Mock engagement criteria
        if (employee.getId() % 3 == 0) {
            score = 64;
            sentiment = "NEUTRAL";
            drivers = Arrays.asList("Work-life balance concerns", "Role ambiguity", "Desire for higher compensation");
        } else if (employee.getId() % 2 == 0) {
            score = 88;
            sentiment = "POSITIVE";
            drivers = Arrays.asList("High alignment with team goals", "Supportive manager relations", "Autonomy in work");
        } else {
            score = 92;
            sentiment = "POSITIVE";
            drivers = Arrays.asList("Excellent professional development opportunities", "Strong peer relationships", "Challenging tasks");
        }

        return AiInsightsDto.EmployeeEngagement.builder()
                .engagementScore(score)
                .sentiment(sentiment)
                .keyDrivers(drivers)
                .build();
    }
}
