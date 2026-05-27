package com.msa.user_service.controller;

import com.msa.user_service.dto.InternalHubManagerResponse;
import com.msa.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/hubs")
@RequiredArgsConstructor
public class HubInternalController {

    private final UserService userService;

    @GetMapping("/{hubId}")
    public ResponseEntity<List<InternalHubManagerResponse>> getHubManagers(
            @PathVariable UUID hubId
    ) {
        return ResponseEntity.ok(userService.getHubManagersByHubId(hubId));
    }
}
