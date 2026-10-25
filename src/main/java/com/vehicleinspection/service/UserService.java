package com.vehicleinspection.service;

import com.vehicleinspection.exception.AuthenticationFailedException;
import com.vehicleinspection.exception.InvalidRequestException;
import com.vehicleinspection.model.User;
import com.vehicleinspection.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

/** MVP tasks 1 and 2 - User Registration and User Login. */
@Service
public class UserService {

    private static final Set<String> ROLES = Set.of("APPLICANT", "REVIEWER");

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(User user) {

        user.setEmail(user.getEmail().trim().toLowerCase());

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new InvalidRequestException("email: is already registered");
        }

        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("APPLICANT");
        }
        user.setRole(user.getRole().trim().toUpperCase());
        if (!ROLES.contains(user.getRole())) {
            throw new InvalidRequestException("role: must be APPLICANT or REVIEWER");
        }

        user.setId(null);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationFailedException("Invalid email or password");
        }
        return user;
    }
}
