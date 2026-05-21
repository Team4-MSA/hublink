package com.msa.delivery_service.presentation;

import com.msa.core_common.response.paging.PageRes;
import com.msa.delivery_service.application.DeliveryService;
import com.msa.delivery_service.presentation.dto.DeliveryDetailResponse;
import com.msa.delivery_service.presentation.dto.DeliveryResponse;
import com.msa.delivery_service.presentation.dto.DeliveryRouteHistoryRequest;
import com.msa.delivery_service.presentation.dto.DeliveryRouteHistoryResponse;
import com.msa.delivery_service.presentation.dto.DeliveryRouteStatusUpdateRequest;
import com.msa.delivery_service.presentation.dto.DeliveryStatusUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/deliveries")
    public PageRes<DeliveryResponse> getDeliveries(
            @RequestHeader("X-User-Role") String role,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return deliveryService.getDeliveries(role, pageable);
    }

    @GetMapping("/deliveries/me")
    public PageRes<DeliveryResponse> getMyDeliveries(
            @RequestHeader("X-User-Id") UUID userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return deliveryService.getMyDeliveries(userId, pageable);
    }

    @GetMapping("/deliveries/{deliveryId}")
    public DeliveryDetailResponse getDelivery(@PathVariable UUID deliveryId) {
        return deliveryService.getDelivery(deliveryId);
    }

    @GetMapping("/orders/{orderId}/deliveries")
    public DeliveryResponse getDeliveryByOrderId(@PathVariable UUID orderId) {
        return deliveryService.getDeliveryByOrderId(orderId);
    }

    @PatchMapping("/deliveries/{deliveryId}/status")
    public DeliveryResponse updateDeliveryStatus(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID deliveryId,
            @Valid @RequestBody DeliveryStatusUpdateRequest request
    ) {
        return deliveryService.updateDeliveryStatus(userId, role, deliveryId, request);
    }

    @PostMapping("/deliveries/{deliveryId}/route-histories")
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryRouteHistoryResponse addRouteHistory(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID deliveryId,
            @Valid @RequestBody DeliveryRouteHistoryRequest request
    ) {
        return deliveryService.addRouteHistory(userId, role, deliveryId, request);
    }

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
