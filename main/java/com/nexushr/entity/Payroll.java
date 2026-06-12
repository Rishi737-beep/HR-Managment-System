package com.nexushr.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payrolls", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "month", "year"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "basic_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal basicSalary;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal allowances;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal deductions;

    @Column(name = "net_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal netSalary;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calculateNetSalary();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateNetSalary();
    }

    public void calculateNetSalary() {
        if (basicSalary == null) basicSalary = BigDecimal.ZERO;
        if (allowances == null) allowances = BigDecimal.ZERO;
        if (deductions == null) deductions = BigDecimal.ZERO;
        this.netSalary = basicSalary.add(allowances).subtract(deductions);
    }
}
