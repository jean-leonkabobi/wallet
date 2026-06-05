package com.kabobi.wallet.controller;

import com.kabobi.wallet.dto.JwtResponse;
import com.kabobi.wallet.dto.SignInRequest;
import com.kabobi.wallet.dto.SignUpRequest;
import com.kabobi.wallet.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody SignInRequest signInRequest) {
        JwtResponse jwtResponse = authService.authenticateUser(signInRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<JwtResponse> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        JwtResponse jwtResponse = authService.registerUser(signUpRequest);
        return ResponseEntity.ok(jwtResponse);
    }
}