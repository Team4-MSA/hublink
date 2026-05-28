package com.msa.delivery_service.controller;

import com.msa.core_common.response.paging.PageRes;
import com.msa.delivery_service.service.DeliveryService;
import com.msa.delivery_service.dto.DeliveryDetailResponse;
import com.msa.delivery_service.dto.DeliveryResponse;
import com.msa.delivery_service.dto.DeliveryRouteHistoryResponse;
import com.msa.delivery_service.dto.DeliveryRouteStatusUpdateRequest;
import com.msa.delivery_service.dto.DeliveryStatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Delivery", description = "Delivery query and status update API")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @Operation(summary = "Get all deliveries")
    @GetMapping("/deliveries")
    public PageRes<DeliveryResponse> getDeliveries(
            @RequestHeader("X-User-Role") String role,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return deliveryService.getDeliveries(role, pageable);
    }

    @Operation(summary = "Get my deliveries")
    @GetMapping("/deliveries/me")
    public PageRes<DeliveryResponse> getMyDeliveries(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return deliveryService.getMyDeliveries(userId, role, pageable);
    }

    @Operation(summary = "Get delivery details")
    @GetMapping("/deliveries/{deliveryId}")
    public DeliveryDetailResponse getDelivery(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID deliveryId
    ) {
        return deliveryService.getDelivery(userId, role, deliveryId);
    }

    @Operation(summary = "Get delivery by order id")
    @GetMapping("/orders/{orderId}/deliveries")
    public DeliveryResponse getDeliveryByOrderId(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID orderId
    ) {
        return deliveryService.getDeliveryByOrderId(role, orderId);
    }

    @Operation(summary = "Update delivery status")
    @PatchMapping("/deliveries/{deliveryId}/status")
    public DeliveryResponse updateDeliveryStatus(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID deliveryId,
            @Valid @RequestBody DeliveryStatusUpdateRequest request
    ) {
        return deliveryService.updateDeliveryStatus(userId, role, deliveryId, request);
    }

    @Operation(summary = "Update route history status")
    @PatchMapping("/delivery-route-histories/{routeHistoryId}/status")
    public DeliveryRouteHistoryResponse updateRouteHistoryStatus(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID routeHistoryId,
            @Valid @RequestBody DeliveryRouteStatusUpdateRequest request
    ) {
        return deliveryService.updateRouteHistoryStatus(userId, role, routeHistoryId, request);
    }
}
