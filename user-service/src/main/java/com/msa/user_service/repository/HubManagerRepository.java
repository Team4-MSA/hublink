package com.msa.user_service.repository;

import com.msa.user_service.entity.HubManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HubManagerRepository extends JpaRepository<HubManager, UUID> {

    Optional<HubManager> findByHubManagerIdAndDeletedAtIsNull(UUID hubManagerId);

    Optional<HubManager> findByUserIdAndDeletedAtIsNull(UUID userId);

    Page<HubManager> findAllByDeletedAtIsNull(Pageable pageable);

    Page<HubManager> findAllByHubIdAndDeletedAtIsNull(UUID hubId, Pageable pageable);

    boolean existsByUserIdAndDeletedAtIsNull(UUID userId);

    boolean existsByUserIdAndHubIdAndDeletedAtIsNull(UUID userId, UUID hubId);

    List<HubManager> findAllByUserIdAndDeletedAtIsNull(UUID userId);

    Optional<HubManager> findByHubIdAndDeletedAtIsNull(UUID hubId);
}
