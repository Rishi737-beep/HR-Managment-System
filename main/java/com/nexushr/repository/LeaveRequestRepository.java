package com.nexushr.repository;

import com.nexushr.entity.LeaveRequest;
import com.nexushr.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    
    List<LeaveRequest> findByStatus(LeaveStatus status);
    
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.status = :status AND :date BETWEEN lr.startDate AND lr.endDate")
    long countEmployeesOnLeaveOnDate(@Param("date") LocalDate date, @Param("status") LeaveStatus status);
    
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employee.manager.id = :managerId AND lr.status = :status")
    long countPendingRequestsForManager(@Param("managerId") Long managerId, @Param("status") LeaveStatus status);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.manager.id = :managerId")
    List<LeaveRequest> findByManagerId(@Param("managerId") Long managerId);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.status = :status AND lr.startDate >= :startOfYear AND lr.endDate <= :endOfYear")
    List<LeaveRequest> findApprovedLeavesForYear(@Param("employeeId") Long employeeId, @Param("status") LeaveStatus status, @Param("startOfYear") LocalDate startOfYear, @Param("endOfYear") LocalDate endOfYear);
}
