package com.msa.hub_service.repository;

import com.msa.hub_service.entity.HubRouteEntity;
import com.msa.hub_service.entity.RouteType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface HubRouteRepositoryCustom {
    // 검색
    Page<HubRouteEntity> searchHubRoutes(UUID departureHubId, UUID arrivalHubId, RouteType routeType, Pageable pageable);

    // 해당 아이디가 출발이거나 도착인 것 조회
    List<HubRouteEntity> findByInvolvedHubId(UUID hubId);

    // 경유 경로 조회
    List<HubRouteEntity> findOptimalTransitRoute(UUID departureHubId, UUID arrivalHubId, BigDecimal directDistance);
}
