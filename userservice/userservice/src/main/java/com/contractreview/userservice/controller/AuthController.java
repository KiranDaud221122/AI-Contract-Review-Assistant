package com.contractreview.userservice.controller;

import com.contractreview.userservice.dto.LoginRequest;
import com.contractreview.userservice.dto.RegisterRequest;
import com.contractreview.userservice.dto.UserResponse;
import com.contractreview.userservice.service.AuthService;
import com.contractreview.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(201).body(user);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
