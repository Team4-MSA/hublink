package com.msa.user_service.controller;

import com.msa.user_service.dto.InternalHubManagerResponse;
import com.msa.user_service.service.UserService;
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

    private final UserService userService;

    @GetMapping("/{hubId}")
    public ResponseEntity<InternalHubManagerResponse> getHubManager(
            @PathVariable UUID hubId
    ) {
        return ResponseEntity.ok(userService.getHubManagerByHubId(hubId));
    }
}
