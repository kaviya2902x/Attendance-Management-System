package com.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaves")
@Data
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "leave_type", nullable = false)
    private String leaveType; // SICK, CASUAL, EARNED

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    private Integer totalDays;
    private String reason;
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "approved_by")
    private String approvedBy;

    private String comments;

    @Column(name = "applied_on")
    private LocalDateTime appliedOn;

    @Column(name = "processed_on")
    private LocalDateTime processedOn;

    @PrePersist
    protected void onCreate() {
        appliedOn = LocalDateTime.now();
        if (totalDays == null && startDate != null && endDate != null) {
            totalDays = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
    }
}