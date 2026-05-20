package com.msa.hub_service.repository;

import com.msa.hub_service.entity.HubRouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HubRouteRepository extends JpaRepository<HubRouteEntity, UUID> {
}
