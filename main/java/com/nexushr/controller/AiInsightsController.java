package com.nexushr.controller;

import com.nexushr.dto.AiInsightsDto;
import com.nexushr.security.EmployeePrincipal;
import com.nexushr.service.AttritionPredictionService;
import com.nexushr.service.EmployeeEngagementService;
import com.nexushr.service.SkillGapAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "AI Insights Module", description = "Endpoints for fetching workforce intelligence models (attrition risk, skill gap analysis, and engagement score)")
public class AiInsightsController {

    private final AttritionPredictionService attritionPredictionService;
    private final SkillGapAnalysisService skillGapAnalysisService;
    private final EmployeeEngagementService employeeEngagementService;

    public AiInsightsController(AttritionPredictionService attritionPredictionService,
                                SkillGapAnalysisService skillGapAnalysisService,
                                EmployeeEngagementService employeeEngagementService) {
        this.attritionPredictionService = attritionPredictionService;
        this.skillGapAnalysisService = skillGapAnalysisService;
        this.employeeEngagementService = employeeEngagementService;
    }

    @GetMapping("/attrition/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Predict employee attrition risk", description = "Admin/HR/Manager only. Calculates mock risk factor and confidence percentage.")
    public ResponseEntity<AiInsightsDto.AttritionPrediction> predictAttrition(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attritionPredictionService.predictAttrition(employeeId));
    }

    @GetMapping("/skill-gap/{employeeId}")
    @Operation(summary = "Perform employee skill gap analysis", description = "Admin/HR/Manager can query anyone. Regular employees can only query their own report.")
    public ResponseEntity<AiInsightsDto.SkillGapAnalysis> analyzeSkillGap(
            @AuthenticationPrincipal EmployeePrincipal principal,
            @PathVariable Long employeeId
    ) {
        Long targetEmployeeId = employeeId;
        boolean isPrivileged = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_HR") ||
                        a.getAuthority().equals("ROLE_MANAGER"));

        if (!isPrivileged) {
            targetEmployeeId = principal.getId();
        }

        return ResponseEntity.ok(skillGapAnalysisService.analyzeSkillGap(targetEmployeeId));
    }

    @GetMapping("/engagement/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Assess employee engagement score", description = "Admin/HR/Manager only. Returns a simulated engagement metric, sentiment, and key drivers.")
    public ResponseEntity<AiInsightsDto.EmployeeEngagement> assessEngagement(@PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeEngagementService.assessEngagement(employeeId));
    }
}
