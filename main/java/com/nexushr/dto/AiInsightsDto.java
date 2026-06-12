package com.nexushr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public final class AiInsightsDto {

    private AiInsightsDto() {}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttritionPrediction {
        private String attritionRisk; // HIGH, MEDIUM, LOW
        private Integer confidence; // percentage e.g. 88
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SkillGapAnalysis {
        private List<String> skillsIdentified;
        private List<String> skillsRequired;
        private List<String> gaps;
        private List<String> recommendations;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeeEngagement {
        private Integer engagementScore; // 1-100 scale
        private String sentiment; // POSITIVE, NEUTRAL, NEGATIVE
        private List<String> keyDrivers;
    }
}
