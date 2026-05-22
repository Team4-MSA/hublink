package com.msa.user_service.controller;

import com.msa.user_service.dto.UserAuthResponse;
import com.msa.user_service.dto.VerifyResponse;
import com.msa.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserService userService;

    @GetMapping("/username/{username}")
    public ResponseEntity<UserAuthResponse> getUserByUsername(
            @PathVariable String username
    ) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping("/id/{userId}")
    public ResponseEntity<UserAuthResponse> getUserById(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/{userId}/hub/{hubId}/verify")
    public ResponseEntity<VerifyResponse> verifyHub(
            @PathVariable UUID userId,
            @PathVariable UUID hubId
    ) {
        return ResponseEntity.ok(VerifyResponse.of(userService.verifyHub(userId, hubId)));
    }

    @GetMapping("/{userId}/company/{companyId}/verify")
    public ResponseEntity<VerifyResponse> verifyCompany(
            @PathVariable UUID userId,
            @PathVariable UUID companyId
    ) {
        return ResponseEntity.ok(VerifyResponse.of(userService.verifyCompany(userId, companyId)));
    }
}
