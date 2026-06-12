package com.nexushr.service.impl;

import com.nexushr.dto.LeaveDto;
import com.nexushr.entity.*;
import com.nexushr.exception.BadRequestException;
import com.nexushr.exception.ResourceNotFoundException;
import com.nexushr.repository.EmployeeRepository;
import com.nexushr.repository.LeaveRequestRepository;
import com.nexushr.service.LeaveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    // Standard Leave Entitlements
    private static final Map<LeaveType, Integer> ENTITLEMENTS = new EnumMap<>(LeaveType.class);
    static {
        ENTITLEMENTS.put(LeaveType.CASUAL, 10);
        ENTITLEMENTS.put(LeaveType.SICK, 10);
        ENTITLEMENTS.put(LeaveType.VACATION, 15);
        ENTITLEMENTS.put(LeaveType.MATERNITY, 90);
        ENTITLEMENTS.put(LeaveType.PATERNITY, 15);
        ENTITLEMENTS.put(LeaveType.UNPAID, 180);
    }

    public LeaveServiceImpl(LeaveRequestRepository leaveRequestRepository, EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public LeaveDto.Response apply(Long employeeId, LeaveDto.ApplyRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        long requestedDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        
        // Check balance
        List<LeaveDto.BalanceResponse> balanceList = getLeaveBalance(employeeId);
        LeaveDto.BalanceResponse balance = balanceList.stream()
                .filter(b -> b.getLeaveType() == request.getLeaveType())
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Invalid leave type: " + request.getLeaveType()));

        if (balance.getRemaining() < requestedDays) {
            throw new BadRequestException("Insufficient leave balance. Remaining: " + balance.getRemaining() + ", Requested: " + requestedDays);
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public LeaveDto.Response approve(Long id, Long approverId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Leave request is already processed. Current status: " + leaveRequest.getStatus());
        }

        Employee approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found with ID: " + approverId));

        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(approver);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public LeaveDto.Response reject(Long id, Long approverId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Leave request is already processed. Current status: " + leaveRequest.getStatus());
        }

        Employee approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found with ID: " + approverId));

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setApprovedBy(approver);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveDto.Response> getLeaves(Long employeeId, String role) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }

        if (role.equalsIgnoreCase("ROLE_ADMIN") || role.equalsIgnoreCase("ROLE_HR")) {
            return leaveRequestRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else if (role.equalsIgnoreCase("ROLE_MANAGER")) {
            // Find all pending requests under manager + manager's own requests
            List<LeaveRequest> managerLeaves = leaveRequestRepository.findByEmployeeId(employeeId);
            List<LeaveRequest> teamLeaves = leaveRequestRepository.findByManagerId(employeeId);
            
            List<LeaveRequest> combined = new ArrayList<>(managerLeaves);
            combined.addAll(teamLeaves);
            return combined.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else {
            return leaveRequestRepository.findByEmployeeId(employeeId).stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveDto.BalanceResponse> getLeaveBalance(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }

        LocalDate startOfYear = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate endOfYear = LocalDate.of(LocalDate.now().getYear(), 12, 31);

        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findApprovedLeavesForYear(
                employeeId, LeaveStatus.APPROVED, startOfYear, endOfYear);

        Map<LeaveType, Integer> usedMap = new EnumMap<>(LeaveType.class);
        for (LeaveType type : LeaveType.values()) {
            usedMap.put(type, 0);
        }

        for (LeaveRequest leave : approvedLeaves) {
            long days = ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
            usedMap.put(leave.getLeaveType(), usedMap.get(leave.getLeaveType()) + (int) days);
        }

        List<LeaveDto.BalanceResponse> balances = new ArrayList<>();
        for (LeaveType type : LeaveType.values()) {
            int total = ENTITLEMENTS.getOrDefault(type, 0);
            int used = usedMap.getOrDefault(type, 0);
            int remaining = Math.max(0, total - used);

            balances.add(LeaveDto.BalanceResponse.builder()
                    .leaveType(type)
                    .totalEntitled(total)
                    .used(used)
                    .remaining(remaining)
                    .build());
        }

        return balances;
    }

    private LeaveDto.Response mapToResponse(LeaveRequest leaveRequest) {
        return LeaveDto.Response.builder()
                .id(leaveRequest.getId())
                .employeeId(leaveRequest.getEmployee().getId())
                .employeeName(leaveRequest.getEmployee().getFirstName() + " " + leaveRequest.getEmployee().getLastName())
                .leaveType(leaveRequest.getLeaveType())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .reason(leaveRequest.getReason())
                .status(leaveRequest.getStatus())
                .approvedById(leaveRequest.getApprovedBy() != null ? leaveRequest.getApprovedBy().getId() : null)
                .approvedByName(leaveRequest.getApprovedBy() != null ? (leaveRequest.getApprovedBy().getFirstName() + " " + leaveRequest.getApprovedBy().getLastName()) : null)
                .createdAt(leaveRequest.getCreatedAt())
                .build();
    }
}
