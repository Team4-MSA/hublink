package com.msa.user_service.controller;

import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.dto.DeliveryManagerRequest;
import com.msa.user_service.dto.DeliveryManagerResponse;
import com.msa.user_service.entity.DeliveryManagerType;
import com.msa.user_service.service.DeliveryManagerService;
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
@RequestMapping("/api/v1/delivery-managers")
@RequiredArgsConstructor
public class DeliveryManagerController {

    private final DeliveryManagerService deliveryManagerService;

    @PostMapping
    public ResponseEntity<DeliveryManagerResponse> register(
            @Valid @RequestBody DeliveryManagerRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER") && !role.equals("HUB_MANAGER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deliveryManagerService.register(request, role, UUID.fromString(userId)));
    }

    @GetMapping
    public ResponseEntity<PageRes<DeliveryManagerResponse>> getList(
            @RequestParam(required = false) UUID hubId,
            @RequestParam(required = false) DeliveryManagerType type,
            Pageable pageable,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER") && !role.equals("HUB_MANAGER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        return ResponseEntity.ok(deliveryManagerService.getList(hubId, type, pageable, role, UUID.fromString(userId)));
    }

    @GetMapping("/{targetUserId}")
    public ResponseEntity<DeliveryManagerResponse> getOne(
            @PathVariable UUID targetUserId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER") && !role.equals("HUB_MANAGER") && !role.equals("DELIVERY_MANAGER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        return ResponseEntity.ok(deliveryManagerService.getOne(targetUserId, role, UUID.fromString(userId)));
    }

    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID targetUserId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("MASTER") && !role.equals("HUB_MANAGER")) {
            throw new CustomException(UserErrorCode.ACCESS_DENIED);
        }
        deliveryManagerService.delete(targetUserId, userId, role, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}
