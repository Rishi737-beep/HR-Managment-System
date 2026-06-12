package com.nexushr.service.impl;

import com.nexushr.dto.DashboardDto;
import com.nexushr.entity.AttendanceStatus;
import com.nexushr.entity.Employee;
import com.nexushr.entity.EmployeeStatus;
import com.nexushr.entity.LeaveStatus;
import com.nexushr.exception.ResourceNotFoundException;
import com.nexushr.repository.*;
import com.nexushr.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PayrollRepository payrollRepository;

    public DashboardServiceImpl(EmployeeRepository employeeRepository,
                                AttendanceRepository attendanceRepository,
                                LeaveRequestRepository leaveRequestRepository,
                                PayrollRepository payrollRepository) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.payrollRepository = payrollRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardDto.AdminDashboard getAdminDashboard() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        long total = employeeRepository.count();
        long active = employeeRepository.countByStatus(EmployeeStatus.ACTIVE);

        long present = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.PRESENT);
        long late = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.LATE);
        long halfDay = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.HALFDAY);
        long presentToday = present + late + halfDay;

        long leaves = leaveRequestRepository.countEmployeesOnLeaveOnDate(today, LeaveStatus.APPROVED);

        BigDecimal cost = payrollRepository.sumNetSalaryByMonthAndYear(month, year);
        if (cost == null) {
            cost = BigDecimal.ZERO;
        }

        return DashboardDto.AdminDashboard.builder()
                .totalEmployees(total)
                .activeEmployees(active)
                .presentToday(presentToday)
                .employeesOnLeave(leaves)
                .monthlyPayrollCost(cost)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardDto.ManagerDashboard getManagerDashboard(Long managerId) {
        if (!employeeRepository.existsById(managerId)) {
            throw new ResourceNotFoundException("Manager not found with ID: " + managerId);
        }

        LocalDate today = LocalDate.now();

        long teamSize = employeeRepository.countByManagerId(managerId);
        long pendingLeaves = leaveRequestRepository.countPendingRequestsForManager(managerId, LeaveStatus.PENDING);

        double rate = 0.0;
        if (teamSize > 0) {
            long present = attendanceRepository.countTeamAttendanceByDateAndStatus(managerId, today, AttendanceStatus.PRESENT);
            long late = attendanceRepository.countTeamAttendanceByDateAndStatus(managerId, today, AttendanceStatus.LATE);
            long halfDay = attendanceRepository.countTeamAttendanceByDateAndStatus(managerId, today, AttendanceStatus.HALFDAY);
            long totalPresent = present + late + halfDay;

            rate = ((double) totalPresent / teamSize) * 100.0;
        }

        return DashboardDto.ManagerDashboard.builder()
                .teamSize(teamSize)
                .attendanceRate(rate)
                .pendingLeaveRequests(pendingLeaves)
                .build();
    }
}
