package com.vehicleinspection.controller;

import com.vehicleinspection.dto.LoginRequest;
import com.vehicleinspection.dto.LoginResponse;
import com.vehicleinspection.model.User;
import com.vehicleinspection.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody User user) {

        User registeredUser = userService.registerUser(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        User user = userService.login(request.email(), request.password());

        return ResponseEntity.ok(new LoginResponse(
                user.getId(), user.getName(), user.getEmail(), user.getRole(),
                "Login successful"));
    }
}
