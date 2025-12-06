package com.attendance.service;

import com.attendance.entity.Regularization;
import com.attendance.entity.User;
import com.attendance.repository.RegularizationRepository;
import com.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RegularizationService {

    @Autowired
    private RegularizationRepository regularizationRepository;

    @Autowired
    private UserRepository userRepository;

    public Regularization requestRegularization(Regularization regularization) {
        User user = userRepository.findById(regularization.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        regularization.setUser(user);
        return regularizationRepository.save(regularization);
    }

    public List<Regularization> getUserRegularizations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return regularizationRepository.findByUser(user);
    }

    public List<Regularization> getPendingRegularizations() {
        return regularizationRepository.findPendingRegularizations();
    }

    // Add this missing method
    public List<Regularization> getAllRegularizations() {
        return regularizationRepository.findAll();
    }

    public Regularization approveRegularization(Long id, String approvedBy, String comments) {
        Regularization regularization = regularizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regularization not found"));

        regularization.setStatus("APPROVED");
        regularization.setApprovedBy(approvedBy);
        regularization.setComments(comments);
        regularization.setProcessedOn(LocalDateTime.now());

        return regularizationRepository.save(regularization);
    }

    public Regularization rejectRegularization(Long id, String rejectedBy, String comments) {
        Regularization regularization = regularizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regularization not found"));

        regularization.setStatus("REJECTED");
        regularization.setApprovedBy(rejectedBy);
        regularization.setComments(comments);
        regularization.setProcessedOn(LocalDateTime.now());

        return regularizationRepository.save(regularization);
    }

    public long countPendingRegularizations() {
        return regularizationRepository.findPendingRegularizations().size();
    }
}