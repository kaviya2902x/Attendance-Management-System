package com.attendance.repository;

import com.attendance.entity.Attendance;
import com.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserAndAttendanceDate(User user, LocalDate date);
    List<Attendance> findByUser(User user);
    List<Attendance> findByUserAndAttendanceDateBetween(User user, LocalDate start, LocalDate end);
    List<Attendance> findByAttendanceDate(LocalDate date);
    List<Attendance> findByAttendanceDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.user = :user AND a.status = 'PRESENT' " +
            "AND MONTH(a.attendanceDate) = :month AND YEAR(a.attendanceDate) = :year")
    Long countPresentDays(@Param("user") User user, @Param("month") int month, @Param("year") int year);

    @Query("SELECT a FROM Attendance a WHERE a.punchIn IS NOT NULL AND a.punchOut IS NULL")
    List<Attendance> findActiveSessions();
}