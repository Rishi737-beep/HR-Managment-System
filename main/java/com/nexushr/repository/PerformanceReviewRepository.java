package com.nexushr.repository;

import com.nexushr.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    
    List<PerformanceReview> findByEmployeeIdOrderByReviewDateDesc(Long employeeId);
    
    List<PerformanceReview> findByReviewerId(Long reviewerId);
}
