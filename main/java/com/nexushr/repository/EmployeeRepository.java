package com.nexushr.repository;

import com.nexushr.entity.Employee;
import com.nexushr.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByEmail(String email);
    
    Optional<Employee> findByEmployeeCode(String employeeCode);
    
    boolean existsByEmail(String email);
    
    boolean existsByEmployeeCode(String employeeCode);
    
    Optional<Employee> findByRefreshToken(String refreshToken);
    
    List<Employee> findByManagerId(Long managerId);
    
    long countByManagerId(Long managerId);
    
    long countByStatus(EmployeeStatus status);
    
    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.designation) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Employee> searchEmployees(@Param("query") String query, Pageable pageable);
}
