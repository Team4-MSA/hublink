package com.msa.delivery_service.presentation;

import com.msa.delivery_service.application.DeliveryService;
import com.msa.delivery_service.presentation.dto.DeliveryRequest;
import com.msa.delivery_service.presentation.dto.DeliveryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/deliveries")
public class DeliveryInternalController {

    private final DeliveryService deliveryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryResponse createDelivery(@Valid @RequestBody DeliveryRequest request) {
        return deliveryService.createDelivery(request);
    }
}
