package com.msa.hub_service.repository;

import com.msa.hub_service.entity.HubRouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HubRouteRepository extends JpaRepository<HubRouteEntity, UUID>, HubRouteRepositoryCustom {

    boolean existsByDepartureHub_HubIdAndArrivalHub_HubId(UUID departureHub_hubId, UUID arrivalHub_hubId);

}
