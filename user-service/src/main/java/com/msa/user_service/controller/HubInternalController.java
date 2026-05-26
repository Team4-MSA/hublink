package com.msa.user_service.controller;

import com.msa.core_common.error.exception.CustomException;
import com.msa.user_service.dto.InternalHubManagerResponse;
import com.msa.user_service.entity.UserRole;
import com.msa.user_service.global.UserErrorCode;
import com.msa.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/hubs")
@RequiredArgsConstructor
public class HubInternalController {

    private final UserRepository userRepository;

    @GetMapping("/{hubId}")
    public ResponseEntity<InternalHubManagerResponse> getHubManager(
            @PathVariable UUID hubId
    ) {
        return userRepository.findByHubIdAndDeletedAtIsNull(hubId)
                .filter(user -> user.getRole() == UserRole.HUB_MANAGER)
                .map(user -> ResponseEntity.ok(InternalHubManagerResponse.of(user)))
                .orElseThrow(() -> new CustomException(UserErrorCode.HUB_MANAGER_NOT_FOUND));
    }
}
