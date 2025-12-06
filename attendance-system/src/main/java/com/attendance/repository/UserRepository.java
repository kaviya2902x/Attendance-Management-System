package com.attendance.repository;

import com.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email); // Make sure this method exists
    Optional<User> findByEmployeeId(String employeeId);
    List<User> findByRole(String role);
    List<User> findByDepartment(String department);
    List<User> findByActive(boolean active);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "u.employeeId LIKE CONCAT('%', :search, '%') OR " +
            "u.username LIKE CONCAT('%', :search, '%')")
    List<User> searchUsers(@Param("search") String search);
}