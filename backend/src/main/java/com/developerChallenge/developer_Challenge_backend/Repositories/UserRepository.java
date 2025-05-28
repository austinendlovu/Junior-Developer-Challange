package com.developerChallenge.developer_Challenge_backend.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.developerChallenge.developer_Challenge_backend.Models.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.role = 'TEACHER' AND u.id = :id")
    Optional<User> findTeacherById(@Param("id") Long id);
}
