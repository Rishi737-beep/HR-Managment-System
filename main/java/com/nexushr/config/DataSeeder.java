package com.nexushr.config;

import com.nexushr.entity.*;
import com.nexushr.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(DepartmentRepository departmentRepository,
                      EmployeeRepository employeeRepository,
                      PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (departmentRepository.count() == 0) {
            log.info("Seeding database departments...");
            Department engineering = Department.builder()
                    .name("Engineering")
                    .description("Software engineering and technology systems development")
                    .build();

            Department hr = Department.builder()
                    .name("Human Resources")
                    .description("Talent management, recruitment, and employee relations")
                    .build();

            Department sales = Department.builder()
                    .name("Sales")
                    .description("Business development, client management, and sales operations")
                    .build();

            departmentRepository.save(engineering);
            departmentRepository.save(hr);
            departmentRepository.save(sales);
            log.info("Departments seeded successfully.");
        }

        if (employeeRepository.count() == 0) {
            log.info("Seeding database employees...");
            Department engineering = departmentRepository.findByName("Engineering").orElse(null);
            Department hr = departmentRepository.findByName("Human Resources").orElse(null);

            // 1. Create Admin
            Employee admin = Employee.builder()
                    .employeeCode("EMP001")
                    .firstName("Nexus")
                    .lastName("Admin")
                    .email("admin@nexushr.com")
                    .phone("1234567890")
                    .designation("Senior Architect")
                    .department(engineering)
                    .joiningDate(LocalDate.of(2023, 1, 15))
                    .salary(BigDecimal.valueOf(150000.00))
                    .status(EmployeeStatus.ACTIVE)
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();

            Employee savedAdmin = employeeRepository.save(admin);

            // 2. Create HR
            Employee hrSpecialist = Employee.builder()
                    .employeeCode("EMP002")
                    .firstName("Sarah")
                    .lastName("HR")
                    .email("hr@nexushr.com")
                    .phone("1234567891")
                    .designation("HR Director")
                    .department(hr)
                    .joiningDate(LocalDate.of(2023, 6, 1))
                    .salary(BigDecimal.valueOf(95000.00))
                    .status(EmployeeStatus.ACTIVE)
                    .passwordHash(passwordEncoder.encode("hr12345"))
                    .role(Role.HR)
                    .build();

            employeeRepository.save(hrSpecialist);

            // 3. Create Manager
            Employee manager = Employee.builder()
                    .employeeCode("EMP003")
                    .firstName("John")
                    .lastName("Manager")
                    .email("manager@nexushr.com")
                    .phone("1234567892")
                    .designation("Engineering Lead")
                    .department(engineering)
                    .joiningDate(LocalDate.of(2023, 3, 10))
                    .salary(BigDecimal.valueOf(120000.00))
                    .status(EmployeeStatus.ACTIVE)
                    .passwordHash(passwordEncoder.encode("manager123"))
                    .role(Role.MANAGER)
                    .build();

            Employee savedManager = employeeRepository.save(manager);

            // 4. Create Employee under Manager
            Employee developer = Employee.builder()
                    .employeeCode("EMP004")
                    .firstName("David")
                    .lastName("Developer")
                    .email("employee@nexushr.com")
                    .phone("1234567893")
                    .designation("Software Developer")
                    .department(engineering)
                    .joiningDate(LocalDate.of(2024, 1, 10))
                    .salary(BigDecimal.valueOf(80000.00))
                    .status(EmployeeStatus.ACTIVE)
                    .manager(savedManager)
                    .passwordHash(passwordEncoder.encode("employee123"))
                    .role(Role.EMPLOYEE)
                    .build();

            employeeRepository.save(developer);

            log.info("Employees seeded successfully.");
        }
    }
}
