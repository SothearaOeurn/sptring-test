package com.theara.postgres.service;

import com.theara.postgres.model.response.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> getAllUsers();

    Optional<User> getUserId(Long id);

    User saveUser(User user);

    void deleteUser(Long id);

    Optional<User> getUserByEmail(String email);

    Optional<User> getUserByUserId(String userId);
}
