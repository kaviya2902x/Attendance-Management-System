package com.attendance.service;

import com.attendance.entity.Leave;
import com.attendance.entity.User;
import com.attendance.repository.LeaveRepository;
import com.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private UserRepository userRepository;

    public Leave applyLeave(Leave leave) {
        User user = userRepository.findById(leave.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        leave.setUser(user);
        return leaveRepository.save(leave);
    }

    public List<Leave> getUserLeaves(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return leaveRepository.findByUser(user);
    }

    public List<Leave> getPendingLeaves() {
        return leaveRepository.findPendingLeaves();
    }

    public List<Leave> getAllLeaves() {
        return leaveRepository.findAll();
    }

    public Leave approveLeave(Long leaveId, String approvedBy, String comments) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        leave.setStatus("APPROVED");
        leave.setApprovedBy(approvedBy);
        leave.setComments(comments);
        leave.setProcessedOn(LocalDateTime.now());

        return leaveRepository.save(leave);
    }

    public Leave rejectLeave(Long leaveId, String rejectedBy, String comments) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        leave.setStatus("REJECTED");
        leave.setApprovedBy(rejectedBy);
        leave.setComments(comments);
        leave.setProcessedOn(LocalDateTime.now());

        return leaveRepository.save(leave);
    }

    public long countPendingLeaves() {
        return leaveRepository.findPendingLeaves().size();
    }
}