package com.msa.delivery_service.infrastructure.repository;

import com.msa.delivery_service.domain.entity.DeliveryRouteHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeliveryRouteHistoryRepository extends JpaRepository<DeliveryRouteHistory, UUID> {
}
