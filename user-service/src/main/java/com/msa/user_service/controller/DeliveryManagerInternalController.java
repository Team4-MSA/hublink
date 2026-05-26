package com.msa.user_service.controller;

import com.msa.user_service.dto.InternalDeliveryManagerResponse;
import com.msa.user_service.service.DeliveryManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/delivery-managers")
@RequiredArgsConstructor
public class DeliveryManagerInternalController {

    private final DeliveryManagerService deliveryManagerService;

    @GetMapping("/search")
    public ResponseEntity<List<InternalDeliveryManagerResponse>> getDeliveryManagersByHubs(
            @RequestParam List<UUID> hubIds
    ) {
        return ResponseEntity.ok(deliveryManagerService.getDeliveryManagersByHubsForInternal(hubIds));
    }
}
