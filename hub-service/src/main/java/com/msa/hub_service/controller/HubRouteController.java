package com.msa.hub_service.controller;

import com.msa.core_common.response.paging.PageRes;
import com.msa.hub_service.dto.HubRouteRequest;
import com.msa.hub_service.dto.HubRouteResponse;
import com.msa.hub_service.dto.HubRouteUpdateRequest;
import com.msa.hub_service.entity.RouteType;
import com.msa.hub_service.service.HubRouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/hub-routes")
@RequiredArgsConstructor
public class HubRouteController {
    private final HubRouteService hubRouteService;

    // 루트 등록
    @PostMapping
    public HubRouteResponse createHubRoute(@Valid @RequestBody HubRouteRequest request) {
        return hubRouteService.createHubRoute(request.departureHub(), request.arrivalHub());
    }

    // 상세 조회
    @GetMapping("/{hubRouteId}")
    public HubRouteResponse getHubRoute(@PathVariable UUID hubRouteId) {
        return hubRouteService.getHubRoute(hubRouteId);
    }

    // 수정
    @PatchMapping("/{hubRouteId}")
    public HubRouteResponse updateHubRoute(@PathVariable UUID hubRouteId, @Valid @RequestBody HubRouteUpdateRequest request) {
        return hubRouteService.updateHubRoute(hubRouteId, request);
    }

    // 삭제
    @DeleteMapping("/{hubRouteId}")
    public HubRouteResponse deleteHubRoute(@PathVariable UUID hubRouteId) {
        return hubRouteService.deleteHubRoute(hubRouteId);
    }

    // 출발/도착으로 검색
    @GetMapping
    public PageRes<HubRouteResponse> getHubRoutes(
            @RequestParam(required = false) UUID departureHubId,
            @RequestParam(required = false) UUID arrivalHubId,
            @RequestParam(required = false) RouteType routeType,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return hubRouteService.getHubRoutes(departureHubId, arrivalHubId, routeType, pageable);
    }

    @GetMapping("/path")
    public List<HubRouteResponse> getHubPath(
            @RequestParam UUID departureHubId,
            @RequestParam UUID arrivalHubId
    ) {
        return hubRouteService.getHubPath(departureHubId, arrivalHubId);
    }
}
