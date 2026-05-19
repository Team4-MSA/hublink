package com.msa.user_service.repository;

import com.msa.user_service.entity.DeliveryManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryManagerRepository extends JpaRepository<DeliveryManager, UUID> {

    Optional<DeliveryManager> findByUserIdAndDeletedAtIsNull(UUID userId);

    List<DeliveryManager> findAllByHubIdAndDeletedAtIsNull(UUID hubId);

    boolean existsByUserIdAndDeletedAtIsNull(UUID userId);
}
