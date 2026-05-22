package com.msa.hub_service.repository;

import com.msa.hub_service.entity.HubRouteEntity;
import com.msa.hub_service.entity.RouteType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface HubRouteRepositoryCustom {
    Page<HubRouteEntity> searchHubRoutes(UUID departureHubId, UUID arrivalHubId, RouteType routeType, Pageable pageable);

    List<HubRouteEntity> findByInvolvedHubId(UUID hubId);
}
