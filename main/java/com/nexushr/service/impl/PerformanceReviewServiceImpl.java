package com.nexushr.service.impl;

import com.nexushr.dto.PerformanceDto;
import com.nexushr.entity.Employee;
import com.nexushr.entity.PerformanceReview;
import com.nexushr.exception.BadRequestException;
import com.nexushr.exception.ResourceNotFoundException;
import com.nexushr.repository.EmployeeRepository;
import com.nexushr.repository.PerformanceReviewRepository;
import com.nexushr.service.PerformanceReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerformanceReviewServiceImpl implements PerformanceReviewService {

    private final PerformanceReviewRepository performanceReviewRepository;
    private final EmployeeRepository employeeRepository;

    public PerformanceReviewServiceImpl(PerformanceReviewRepository performanceReviewRepository,
                                        EmployeeRepository employeeRepository) {
        this.performanceReviewRepository = performanceReviewRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public PerformanceDto.Response addReview(Long reviewerId, PerformanceDto.Request request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        Employee reviewer = employeeRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with ID: " + reviewerId));

        PerformanceReview review = PerformanceReview.builder()
                .employee(employee)
                .reviewer(reviewer)
                .rating(request.getRating())
                .feedback(request.getFeedback())
                .reviewDate(request.getReviewDate())
                .build();

        PerformanceReview saved = performanceReviewRepository.save(review);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public PerformanceDto.Response updateReview(Long reviewerId, Long reviewId, PerformanceDto.Request request) {
        PerformanceReview review = performanceReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found with ID: " + reviewId));

        if (!review.getReviewer().getId().equals(reviewerId)) {
            throw new BadRequestException("Only the reviewer who created this review can modify it");
        }

        review.setRating(request.getRating());
        review.setFeedback(request.getFeedback());
        review.setReviewDate(request.getReviewDate());

        PerformanceReview updated = performanceReviewRepository.save(review);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerformanceDto.Response> getReviewHistory(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }
        return performanceReviewRepository.findByEmployeeIdOrderByReviewDateDesc(employeeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PerformanceDto.Response mapToResponse(PerformanceReview review) {
        return PerformanceDto.Response.builder()
                .id(review.getId())
                .employeeId(review.getEmployee().getId())
                .employeeName(review.getEmployee().getFirstName() + " " + review.getEmployee().getLastName())
                .reviewerId(review.getReviewer().getId())
                .reviewerName(review.getReviewer().getFirstName() + " " + review.getReviewer().getLastName())
                .rating(review.getRating())
                .feedback(review.getFeedback())
                .reviewDate(review.getReviewDate())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
