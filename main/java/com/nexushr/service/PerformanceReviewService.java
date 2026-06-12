package com.nexushr.service;

import com.nexushr.dto.PerformanceDto;

import java.util.List;

public interface PerformanceReviewService {
    PerformanceDto.Response addReview(Long reviewerId, PerformanceDto.Request request);
    PerformanceDto.Response updateReview(Long reviewerId, Long reviewId, PerformanceDto.Request request);
    List<PerformanceDto.Response> getReviewHistory(Long employeeId);
}
