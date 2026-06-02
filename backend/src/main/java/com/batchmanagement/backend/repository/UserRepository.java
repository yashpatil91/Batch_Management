package com.batchmanagement.backend.repository;

import com.batchmanagement.backend.entity.User;
import com.batchmanagement.backend.entity.enums.Role;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email
    Optional<User> findByEmail(String email);

    // Check email already exists
    boolean existsByEmail(String email);

    // Get users by role
    List<User> findByRole(Role role);
}