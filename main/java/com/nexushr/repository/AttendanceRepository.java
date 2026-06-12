package com.nexushr.repository;

import com.nexushr.entity.Attendance;
import com.nexushr.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    
    List<Attendance> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);
    
    long countByDateAndStatus(LocalDate date, AttendanceStatus status);
    
    List<Attendance> findByDate(LocalDate date);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.manager.id = :managerId AND a.date = :date AND a.status = :status")
    long countTeamAttendanceByDateAndStatus(@Param("managerId") Long managerId, @Param("date") LocalDate date, @Param("status") AttendanceStatus status);
    
    @Query("SELECT a FROM Attendance a WHERE a.employee.id IN :employeeIds AND a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findByEmployeeIdsAndDateBetween(@Param("employeeIds") List<Long> employeeIds, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
