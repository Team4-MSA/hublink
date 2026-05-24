package com.msa.hub_service.repository;

import com.msa.hub_service.entity.HubRouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HubRouteRepository extends JpaRepository<HubRouteEntity, UUID>, HubRouteRepositoryCustom {

    boolean existsByDepartureHub_HubIdAndArrivalHub_HubId(UUID departureHub_hubId, UUID arrivalHub_hubId);

    // 단일 경로 루트 반환
    Optional<HubRouteEntity> findByDepartureHub_HubIdAndArrivalHub_HubId(UUID departureHubId, UUID arrivalHubId);

}
