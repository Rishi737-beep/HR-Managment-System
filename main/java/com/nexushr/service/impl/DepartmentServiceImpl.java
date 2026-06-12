package com.nexushr.service.impl;

import com.nexushr.dto.DepartmentDto;
import com.nexushr.entity.Department;
import com.nexushr.exception.BadRequestException;
import com.nexushr.exception.ResourceNotFoundException;
import com.nexushr.repository.DepartmentRepository;
import com.nexushr.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public DepartmentDto.Response create(DepartmentDto.Request request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new BadRequestException("Department with name '" + request.getName() + "' already exists");
        }

        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Department saved = departmentRepository.save(department);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DepartmentDto.Response update(Long id, DepartmentDto.Request request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        if (!department.getName().equalsIgnoreCase(request.getName()) && departmentRepository.existsByName(request.getName())) {
            throw new BadRequestException("Department with name '" + request.getName() + "' already exists");
        }

        department.setName(request.getName());
        department.setDescription(request.getDescription());

        Department updated = departmentRepository.save(department);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDto.Response getById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
        return mapToResponse(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto.Response> getAll() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with ID: " + id);
        }
        departmentRepository.deleteById(id);
    }

    private DepartmentDto.Response mapToResponse(Department department) {
        return DepartmentDto.Response.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .build();
    }
}
