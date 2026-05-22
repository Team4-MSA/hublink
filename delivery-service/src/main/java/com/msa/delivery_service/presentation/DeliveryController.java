package com.msa.delivery_service.presentation;

import com.msa.core_common.response.paging.PageRes;
import com.msa.delivery_service.application.DeliveryService;
import com.msa.delivery_service.presentation.dto.DeliveryDetailResponse;
import com.msa.delivery_service.presentation.dto.DeliveryResponse;
import com.msa.delivery_service.presentation.dto.DeliveryRouteHistoryResponse;
import com.msa.delivery_service.presentation.dto.DeliveryRouteStatusUpdateRequest;
import com.msa.delivery_service.presentation.dto.DeliveryStatusUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class DeliveryController {

    private final DeliveryService deliveryService;

    // 전체 배송 목록 조회
    @GetMapping("/deliveries")
    public PageRes<DeliveryResponse> getDeliveries(
            @RequestHeader("X-User-Role") String role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return deliveryService.getDeliveries(role, pageable);
    }

    // 로그인한 배송 담당자의 배송 목록 조회
    @GetMapping("/deliveries/me")
    public PageRes<DeliveryResponse> getMyDeliveries(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return deliveryService.getMyDeliveries(userId, role, pageable);
    }

    // 특정 배송의 기본 정보와 경로 기록 조회
    @GetMapping("/deliveries/{deliveryId}")
    public DeliveryDetailResponse getDelivery(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID deliveryId
    ) {
        return deliveryService.getDelivery(userId, role, deliveryId);
    }

    // 주문 ID로 연결된 배송 정보를 조회
    @GetMapping("/orders/{orderId}/deliveries")
    public DeliveryResponse getDeliveryByOrderId(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID orderId
    ) {
        return deliveryService.getDeliveryByOrderId(role, orderId);
    }

    // 대표 배송 상태 변경
    @PatchMapping("/deliveries/{deliveryId}/status")
    public DeliveryResponse updateDeliveryStatus(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID deliveryId,
            @Valid @RequestBody DeliveryStatusUpdateRequest request
    ) {
        return deliveryService.updateDeliveryStatus(userId, role, deliveryId, request);
    }

    // 특정 배송 경로 이력의 상태 변경
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
