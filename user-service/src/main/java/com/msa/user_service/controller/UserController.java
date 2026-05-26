package com.msa.user_service.controller;

import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.dto.*;
import com.msa.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.msa.core_common.error.exception.CustomException;
import com.msa.user_service.global.UserErrorCode;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(userService.getUser(UUID.fromString(userId)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable UUID userId,
            @RequestHeader("X-User-Id") String requestUserId,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER") && !requestUserId.equals(userId.toString())) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @GetMapping
    public ResponseEntity<PageRes<UserResponse>> getUsers(
            Pageable pageable,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        return ResponseEntity.ok(userService.getUsers(pageable));
    }

    @GetMapping("/pending")
    public ResponseEntity<PageRes<UserResponse>> getPendingUsers(
            Pageable pageable,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        return ResponseEntity.ok(userService.getPendingUsers(pageable));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        userService.validateUpdateResources(request);
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId,
            @RequestHeader("X-User-Id") String requestUserId,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        userService.deleteUser(userId, requestUserId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<Void> approveUser(
            @PathVariable UUID userId,
            @Valid @RequestBody ApproveUserRequest request,
            @RequestHeader("X-User-Id") String requestUserId,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        userService.approveUser(userId, request, UUID.fromString(requestUserId));
        return ResponseEntity.ok().build();
    }
}
