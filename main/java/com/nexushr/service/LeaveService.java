package com.nexushr.service;

import com.nexushr.dto.LeaveDto;

import java.util.List;

public interface LeaveService {
    LeaveDto.Response apply(Long employeeId, LeaveDto.ApplyRequest request);
    LeaveDto.Response approve(Long id, Long approverId);
    LeaveDto.Response reject(Long id, Long approverId);
    List<LeaveDto.Response> getLeaves(Long employeeId, String role);
    List<LeaveDto.BalanceResponse> getLeaveBalance(Long employeeId);
}
