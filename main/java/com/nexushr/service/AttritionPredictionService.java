package com.nexushr.service;

import com.nexushr.dto.AiInsightsDto;

public interface AttritionPredictionService {
    AiInsightsDto.AttritionPrediction predictAttrition(Long employeeId);
}
