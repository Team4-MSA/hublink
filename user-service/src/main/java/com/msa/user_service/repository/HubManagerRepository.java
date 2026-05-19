package com.msa.user_service.repository;

import com.msa.user_service.entity.HubManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HubManagerRepository extends JpaRepository<HubManager, UUID> {

    Optional<HubManager> findByUserIdAndDeletedAtIsNull(UUID userId);

    boolean existsByUserIdAndDeletedAtIsNull(UUID userId);
}
