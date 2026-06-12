package com.nexushr.service.impl;

import com.nexushr.dto.AiInsightsDto;
import com.nexushr.entity.Employee;
import com.nexushr.exception.ResourceNotFoundException;
import com.nexushr.repository.EmployeeRepository;
import com.nexushr.service.SkillGapAnalysisService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class SkillGapAnalysisServiceImpl implements SkillGapAnalysisService {

    private final EmployeeRepository employeeRepository;

    public SkillGapAnalysisServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public AiInsightsDto.SkillGapAnalysis analyzeSkillGap(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        String designation = employee.getDesignation().toLowerCase();

        List<String> identified;
        List<String> required;
        List<String> gaps;
        List<String> recommendations;

        if (designation.contains("developer") || designation.contains("engineer")) {
            identified = Arrays.asList("Java 17", "Spring Boot", "Git", "SQL");
            required = Arrays.asList("Java 21", "Spring Cloud", "Docker", "Kubernetes", "AWS Cloud", "System Design");
            gaps = Arrays.asList("Kubernetes", "AWS Cloud", "Spring Cloud");
            recommendations = Arrays.asList(
                    "Complete 'Kubernetes Certified Administrator' certification",
                    "Attend internal Spring Cloud training workshop",
                    "Enroll in AWS Associate Developer course"
            );
        } else if (designation.contains("manager") || designation.contains("lead")) {
            identified = Arrays.asList("Project Management", "Team Management", "Agile Methodology");
            required = Arrays.asList("Strategic Execution", "Budget Forecasting", "Workforce Analytics", "Conflict Resolution");
            gaps = Arrays.asList("Workforce Analytics", "Budget Forecasting");
            recommendations = Arrays.asList(
                    "Participate in the 'Leadership & Strategic Finance' workshop",
                    "Take the online 'Workforce Intelligence Tools' course"
            );
        } else {
            identified = Arrays.asList("Communication", "MS Office Suite", "Time Management");
            required = Arrays.asList("Domain Knowledge", "Advanced Tools Mastery", "Data Analysis");
            gaps = Arrays.asList("Advanced Tools Mastery", "Data Analysis");
            recommendations = Collections.singletonList("Enroll in professional domain certification program");
        }

        return AiInsightsDto.SkillGapAnalysis.builder()
                .skillsIdentified(identified)
                .skillsRequired(required)
                .gaps(gaps)
                .recommendations(recommendations)
                .build();
    }
}
