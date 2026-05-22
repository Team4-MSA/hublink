package com.msa.user_service.controller;

import com.msa.user_service.dto.InternalDeliveryManagerResponse;
import com.msa.user_service.service.DeliveryManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/delivery-managers")
@RequiredArgsConstructor
public class DeliveryManagerInternalController {

    private final DeliveryManagerService deliveryManagerService;

    @GetMapping
    public ResponseEntity<List<InternalDeliveryManagerResponse>> getDeliveryManagersByHub(
            @RequestParam UUID hubId
    ) {
        return ResponseEntity.ok(deliveryManagerService.getDeliveryManagersByHubForInternal(hubId));
    }
}
