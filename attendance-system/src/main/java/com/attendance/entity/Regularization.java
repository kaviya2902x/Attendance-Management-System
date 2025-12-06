package com.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "regularizations")
@Data
public class Regularization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "requested_punch_in")
    private String requestedPunchIn;

    @Column(name = "requested_punch_out")
    private String requestedPunchOut;

    private String reason;
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "approved_by")
    private String approvedBy;

    private String comments;

    @Column(name = "requested_on")
    private LocalDateTime requestedOn;

    @Column(name = "processed_on")
    private LocalDateTime processedOn;

    @PrePersist
    protected void onCreate() {
        requestedOn = LocalDateTime.now();
    }
}