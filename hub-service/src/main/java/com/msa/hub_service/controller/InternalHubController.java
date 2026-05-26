package com.msa.hub_service.controller;

import com.msa.hub_service.dto.CoordinateDto;
import com.msa.hub_service.service.HubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/hubs")
@RequiredArgsConstructor
public class InternalHubController {

    private final HubService hubService;

    @GetMapping("/{hubId}/exists")
    public boolean getHubExist(@PathVariable UUID hubId) {
        return hubService.getHubExist(hubId);
    }

    @GetMapping("/coordinates")
    public CoordinateDto getCoordinates(@RequestParam("address") String address) {
        return hubService.getCoordinate(address);
    }
}
