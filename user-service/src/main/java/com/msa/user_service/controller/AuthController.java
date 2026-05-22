package com.msa.user_service.controller;

import com.msa.user_service.dto.*;
import com.msa.user_service.service.AuthService;
import com.msa.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.signUp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LogInResponse> login(@Valid @RequestBody LogInRequest request) {
        return ResponseEntity.ok(authService.logIn(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LogInResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader("X-User-Id") String userId
    ) {
        String accessToken = bearerToken.replace("Bearer ", "");
        authService.logOut(accessToken, userId);
        return ResponseEntity.ok().build();
    }
}
