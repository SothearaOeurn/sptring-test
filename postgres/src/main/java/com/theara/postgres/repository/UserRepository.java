package com.theara.postgres.repository;

import com.theara.postgres.model.response.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findUserByUserId(String userId);
}
