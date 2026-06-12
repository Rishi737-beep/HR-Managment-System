package com.nexushr.repository;

import com.nexushr.entity.PaymentStatus;
import com.nexushr.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    
    Optional<Payroll> findByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);
    
    List<Payroll> findByEmployeeIdOrderByYearDescMonthDesc(Long employeeId);
    
    List<Payroll> findByMonthAndYear(Integer month, Integer year);
    
    @Query("SELECT SUM(p.netSalary) FROM Payroll p WHERE p.month = :month AND p.year = :year")
    BigDecimal sumNetSalaryByMonthAndYear(@Param("month") Integer month, @Param("year") Integer year);
    
    boolean existsByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);
}
