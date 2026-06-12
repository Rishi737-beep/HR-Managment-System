package com.nexushr.service;

import com.nexushr.dto.AiInsightsDto;

public interface EmployeeEngagementService {
    AiInsightsDto.EmployeeEngagement assessEngagement(Long employeeId);
}
