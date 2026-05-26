package com.msa.user_service.controller;

import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.dto.HubManagerRequest;
import com.msa.user_service.dto.HubManagerResponse;
import com.msa.user_service.service.HubManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.msa.core_common.error.exception.CustomException;
import com.msa.user_service.global.UserErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hub-managers")
@RequiredArgsConstructor
public class HubManagerController {

    private final HubManagerService hubManagerService;

    @PostMapping
    public ResponseEntity<HubManagerResponse> register(
            @Valid @RequestBody HubManagerRequest request,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hubManagerService.register(request));
    }

    @GetMapping
    public ResponseEntity<PageRes<HubManagerResponse>> getList(
            @RequestParam(required = false) UUID hubId,
            Pageable pageable,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        return ResponseEntity.ok(hubManagerService.getList(hubId, pageable));
    }

    @GetMapping("/{hubManagerId}")
    public ResponseEntity<HubManagerResponse> getOne(
            @PathVariable UUID hubManagerId,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER") && !role.equals("HUB_MANAGER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        return ResponseEntity.ok(hubManagerService.getOne(hubManagerId));
    }

    @DeleteMapping("/{hubManagerId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID hubManagerId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        hubManagerService.delete(hubManagerId, userId);
        return ResponseEntity.noContent().build();
    }
}
