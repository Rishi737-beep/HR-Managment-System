package com.nexushr.service.impl;

import com.nexushr.dto.AttendanceDto;
import com.nexushr.entity.Attendance;
import com.nexushr.entity.AttendanceStatus;
import com.nexushr.entity.Employee;
import com.nexushr.exception.BadRequestException;
import com.nexushr.exception.ResourceNotFoundException;
import com.nexushr.repository.AttendanceRepository;
import com.nexushr.repository.EmployeeRepository;
import com.nexushr.service.AttendanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public AttendanceDto.Response checkIn(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        LocalDate today = LocalDate.now();
        if (attendanceRepository.findByEmployeeIdAndDate(employeeId, today).isPresent()) {
            throw new BadRequestException("Employee already checked in today");
        }

        LocalDateTime checkInTime = LocalDateTime.now();
        
        // Let's assume late check-in if check in is after 09:30 AM
        AttendanceStatus status = AttendanceStatus.PRESENT;
        if (checkInTime.toLocalTime().isAfter(LocalTime.of(9, 30))) {
            status = AttendanceStatus.LATE;
        }

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .date(today)
                .checkInTime(checkInTime)
                .status(status)
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceDto.Response checkOut(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new BadRequestException("No check-in record found for today. Please check in first."));

        if (attendance.getCheckOutTime() != null) {
            throw new BadRequestException("Employee has already checked out today");
        }

        LocalDateTime checkOutTime = LocalDateTime.now();
        attendance.setCheckOutTime(checkOutTime);

        // Calculate working hours
        long seconds = Duration.between(attendance.getCheckInTime(), checkOutTime).toSeconds();
        BigDecimal hours = BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
        attendance.setWorkingHours(hours);

        // Adjust status if they worked less than 4 hours (Half day)
        if (hours.compareTo(BigDecimal.valueOf(4.0)) < 0) {
            attendance.setStatus(AttendanceStatus.HALFDAY);
        }

        Attendance saved = attendanceRepository.save(attendance);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDto.Response> getReport(Long employeeId, LocalDate startDate, LocalDate endDate) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceDto.SummaryResponse getMonthlySummary(Long employeeId, Integer month, Integer year) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Attendance> records = attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, start, end);

        long presentDays = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long lateDays = records.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
        long halfDays = records.stream().filter(r -> r.getStatus() == AttendanceStatus.HALFDAY).count();
        long totalPresentCount = presentDays + lateDays + halfDays;

        // Total working days in month (excluding weekends)
        long workingDaysInMonth = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (current.getDayOfWeek().getValue() < 6) { // Monday to Friday
                workingDaysInMonth++;
            }
            current = current.plusDays(1);
        }

        long absentDays = Math.max(0, workingDaysInMonth - totalPresentCount);

        double attendanceRate = workingDaysInMonth == 0 ? 0.0 :
                ((double) (presentDays + lateDays + (halfDays * 0.5)) / workingDaysInMonth) * 100.0;

        return AttendanceDto.SummaryResponse.builder()
                .month(month)
                .year(year)
                .totalDaysInMonth(workingDaysInMonth)
                .presentDays(presentDays)
                .absentDays(absentDays)
                .lateDays(lateDays)
                .halfDays(halfDays)
                .attendanceRate(Math.min(100.0, Math.max(0.0, attendanceRate)))
                .build();
    }

    private AttendanceDto.Response mapToResponse(Attendance attendance) {
        return AttendanceDto.Response.builder()
                .id(attendance.getId())
                .employeeId(attendance.getEmployee().getId())
                .employeeName(attendance.getEmployee().getFirstName() + " " + attendance.getEmployee().getLastName())
                .date(attendance.getDate())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .workingHours(attendance.getWorkingHours())
                .status(attendance.getStatus())
                .build();
    }
}
