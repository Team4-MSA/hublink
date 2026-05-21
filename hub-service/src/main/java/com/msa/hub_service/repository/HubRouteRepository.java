package com.msa.hub_service.repository;

import com.msa.hub_service.entity.HubRouteEntity;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface HubRouteRepository extends JpaRepository<HubRouteEntity, UUID> {
    boolean existsByDepartureHub_HubIdAndArrivalHub_HubId(UUID departureHub_hubId, UUID arrivalHub_hubId);

    @Query("SELECT r FROM HubRouteEntity r WHERE r.departureHub.hubId = :hubId OR r.arrivalHub.hubId = :hubId")
    List<HubRouteEntity> findByInvolvedHubId(@Param("hubId") UUID hubId);
}
