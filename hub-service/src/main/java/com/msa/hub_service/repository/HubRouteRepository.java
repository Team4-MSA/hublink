package com.msa.hub_service.repository;

import com.msa.hub_service.entity.HubRouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HubRouteRepository extends JpaRepository<HubRouteEntity, UUID> {
    boolean existsByDepartureHub_HubIdAndArrivalHub_HubId(UUID departureHub_hubId, UUID arrivalHub_hubId);

    List<HubRouteEntity> findByDepartureHub_HubIdOrArrivalHub_HubId(UUID departureHubId, UUID arrivalHubId);
}
