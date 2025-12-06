package com.attendance.repository;

import com.attendance.entity.Regularization;
import com.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegularizationRepository extends JpaRepository<Regularization, Long> {
    List<Regularization> findByUser(User user);
    List<Regularization> findByUserAndStatus(User user, String status);
    List<Regularization> findByStatus(String status);

    @Query("SELECT r FROM Regularization r WHERE r.status = 'PENDING' ORDER BY r.requestedOn DESC")
    List<Regularization> findPendingRegularizations();
}