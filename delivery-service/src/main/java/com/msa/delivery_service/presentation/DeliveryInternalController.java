package com.msa.delivery_service.presentation;

import com.msa.delivery_service.application.DeliveryService;
import com.msa.delivery_service.presentation.dto.DeliveryRequest;
import com.msa.delivery_service.presentation.dto.DeliveryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/deliveries")
public class DeliveryInternalController {

    private final DeliveryService deliveryService;

    // 주문 정보를 기반으로 배송과 배송 경로를 생성한다.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryResponse createDelivery(@Valid @RequestBody DeliveryRequest request) {
        return deliveryService.createDelivery(request);
    }
    // 보상 트랜잭션 API
    @PostMapping("/orders/{orderId}/compensate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void compensateDeliveryCreation(@PathVariable UUID orderId) {
        deliveryService.compensateDeliveryCreation(orderId);
    }
}
