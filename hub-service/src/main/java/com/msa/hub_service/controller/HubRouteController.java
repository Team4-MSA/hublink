package com.msa.hub_service.controller;

import com.msa.hub_service.dto.HubRouteRequest;
import com.msa.hub_service.dto.HubRouteResponse;
import com.msa.hub_service.service.HubRouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/hub-routes")
@RequiredArgsConstructor
public class HubRouteController {
    private final HubRouteService hubRouteService;

    //루트 등록
    @PostMapping
    public HubRouteResponse createHubRoute(@Valid @RequestBody HubRouteRequest request) {
        return hubRouteService.createHubRoute(request.departureHub(), request.arrivalHub());
    }

}
