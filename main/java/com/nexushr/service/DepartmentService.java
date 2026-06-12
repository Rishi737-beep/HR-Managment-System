package com.nexushr.service;

import com.nexushr.dto.DepartmentDto;

import java.util.List;

public interface DepartmentService {
    DepartmentDto.Response create(DepartmentDto.Request request);
    DepartmentDto.Response update(Long id, DepartmentDto.Request request);
    DepartmentDto.Response getById(Long id);
    List<DepartmentDto.Response> getAll();
    void delete(Long id);
}
