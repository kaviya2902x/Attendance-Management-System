package com.attendance.repository;

import com.attendance.entity.Leave;
import com.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByUser(User user);
    List<Leave> findByUserAndStatus(User user, String status);
    List<Leave> findByStatus(String status);
    List<Leave> findByLeaveType(String leaveType);

    @Query("SELECT l FROM Leave l WHERE l.status = 'PENDING' ORDER BY l.appliedOn DESC")
    List<Leave> findPendingLeaves();

    @Query("SELECT COUNT(l) FROM Leave l WHERE l.user = :user AND YEAR(l.startDate) = :year " +
            "AND l.leaveType = :leaveType AND l.status = 'APPROVED'")
    Long countApprovedLeavesByType(@Param("user") User user, @Param("year") int year,
                                   @Param("leaveType") String leaveType);
}