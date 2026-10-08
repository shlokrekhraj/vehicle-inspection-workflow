package com.vehicleinspection.controller;

import com.vehicleinspection.model.User;
import com.vehicleinspection.service.UserService;
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
    public ResponseEntity<User> registerUser(@RequestBody User user) {

        User registeredUser = userService.registerUser(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registeredUser);
    }
}