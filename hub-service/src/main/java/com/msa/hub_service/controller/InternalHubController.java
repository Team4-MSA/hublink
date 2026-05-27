package com.msa.hub_service.controller;

import com.msa.hub_service.dto.CoordinateDto;
import com.msa.hub_service.dto.HubRouteResponse;
import com.msa.hub_service.service.HubRouteService;
import com.msa.hub_service.service.HubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalHubController {

    private final HubService hubService;
    private final HubRouteService hubRouteService;

    @GetMapping("/hubs/{hubId}/exists")
    public boolean getHubExist(@PathVariable UUID hubId) {
        return hubService.getHubExist(hubId);
    }

    @GetMapping("/hubs/coordinates")
    public CoordinateDto getCoordinates(@RequestParam("address") String address) {
        return hubService.getCoordinate(address);
    }

    @GetMapping("/hub-routes/path")
    public List<HubRouteResponse> getCompanyPath(
            @RequestParam UUID departureCompanyId,
            @RequestParam UUID arrivalCompanyId
    ){
        return hubRouteService.getCompanyToCompanyPath(departureCompanyId, arrivalCompanyId);
    }
}
