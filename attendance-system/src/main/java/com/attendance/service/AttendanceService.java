package com.attendance.service;

import com.attendance.entity.Attendance;
import com.attendance.entity.User;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    public Attendance punchIn(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        Optional<Attendance> existing = attendanceRepository.findByUserAndAttendanceDate(user, today);

        if (existing.isPresent()) {
            throw new RuntimeException("Already punched in for today");
        }

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setAttendanceDate(today);
        attendance.setPunchIn(LocalDateTime.now());
        attendance.setStatus("PRESENT");

        // Check late arrival (after 9:30 AM)
        LocalTime punchInTime = attendance.getPunchIn().toLocalTime();
        if (punchInTime.isAfter(LocalTime.of(9, 30))) {
            int lateMinutes = (int) java.time.Duration.between(
                    LocalTime.of(9, 30), punchInTime).toMinutes();
            attendance.setLateMinutes(lateMinutes);
        }

        return attendanceRepository.save(attendance);
    }

    public Attendance punchOut(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByUserAndAttendanceDate(user, today)
                .orElseThrow(() -> new RuntimeException("No punch in found for today"));

        if (attendance.getPunchOut() != null) {
            throw new RuntimeException("Already punched out for today");
        }

        attendance.setPunchOut(LocalDateTime.now());

        // Calculate total hours
        if (attendance.getPunchIn() != null && attendance.getPunchOut() != null) {
            long minutes = java.time.Duration.between(
                    attendance.getPunchIn(), attendance.getPunchOut()).toMinutes();
            attendance.setTotalHours(minutes / 60.0);

            // Calculate overtime (more than 8 hours)
            if (attendance.getTotalHours() > 8) {
                attendance.setOvertimeHours(attendance.getTotalHours() - 8);
            }
        }

        return attendanceRepository.save(attendance);
    }

    public Optional<Attendance> getTodayAttendance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return attendanceRepository.findByUserAndAttendanceDate(user, LocalDate.now());
    }

    public List<Attendance> getUserAttendance(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return attendanceRepository.findByUserAndAttendanceDateBetween(user, startDate, endDate);
    }

    public List<Attendance> getAllAttendance(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return attendanceRepository.findByAttendanceDate(date);
    }

    public List<Attendance> getAttendanceByDateRange(LocalDate start, LocalDate end) {
        return attendanceRepository.findByAttendanceDateBetween(start, end);
    }

    public List<Attendance> getActiveSessions() {
        return attendanceRepository.findActiveSessions();
    }

    public Attendance updateAttendance(Long id, Attendance attendanceDetails) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        attendance.setPunchIn(attendanceDetails.getPunchIn());
        attendance.setPunchOut(attendanceDetails.getPunchOut());
        attendance.setStatus(attendanceDetails.getStatus());
        attendance.setNotes(attendanceDetails.getNotes());

        return attendanceRepository.save(attendance);
    }
}