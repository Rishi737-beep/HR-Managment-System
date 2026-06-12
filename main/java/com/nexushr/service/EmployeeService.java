package com.nexushr.service;

import com.nexushr.dto.EmployeeDto;
import org.springframework.data.domain.Page;

public interface EmployeeService {
    EmployeeDto.Response create(EmployeeDto.CreateRequest request);
    EmployeeDto.Response update(Long id, EmployeeDto.UpdateRequest request);
    EmployeeDto.Response getById(Long id);
    Page<EmployeeDto.Response> getAll(int page, int size, String sortBy, String sortDir);
    Page<EmployeeDto.Response> search(String query, int page, int size, String sortBy, String sortDir);
    void delete(Long id);
}
