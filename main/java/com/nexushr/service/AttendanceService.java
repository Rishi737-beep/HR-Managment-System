package com.nexushr.service;

import com.nexushr.dto.AttendanceDto;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceDto.Response checkIn(Long employeeId);
    AttendanceDto.Response checkOut(Long employeeId);
    List<AttendanceDto.Response> getReport(Long employeeId, LocalDate startDate, LocalDate endDate);
    AttendanceDto.SummaryResponse getMonthlySummary(Long employeeId, Integer month, Integer year);
}
