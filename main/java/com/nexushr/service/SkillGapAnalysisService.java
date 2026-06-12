package com.nexushr.service;

import com.nexushr.dto.AiInsightsDto;

public interface SkillGapAnalysisService {
    AiInsightsDto.SkillGapAnalysis analyzeSkillGap(Long employeeId);
}
