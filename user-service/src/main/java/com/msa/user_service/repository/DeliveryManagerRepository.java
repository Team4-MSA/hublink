package com.msa.user_service.repository;

import com.msa.user_service.entity.DeliveryManager;
import com.msa.user_service.entity.DeliveryManagerType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryManagerRepository extends JpaRepository<DeliveryManager, UUID> {

    Optional<DeliveryManager> findByDeliveryManagerIdAndDeletedAtIsNull(UUID deliveryManagerId);

    Optional<DeliveryManager> findByUserIdAndDeletedAtIsNull(UUID userId);

    Page<DeliveryManager> findAllByDeletedAtIsNull(Pageable pageable);

    Page<DeliveryManager> findAllByHubIdAndDeletedAtIsNull(UUID hubId, Pageable pageable);

    Page<DeliveryManager> findAllByTypeAndDeletedAtIsNull(DeliveryManagerType type, Pageable pageable);

    Page<DeliveryManager> findAllByHubIdAndTypeAndDeletedAtIsNull(UUID hubId, DeliveryManagerType type, Pageable pageable);

    List<DeliveryManager> findAllByHubIdAndDeletedAtIsNull(UUID hubId);

    boolean existsByUserIdAndDeletedAtIsNull(UUID userId);

    Page<DeliveryManager> findAllByHubIdInAndDeletedAtIsNull(Collection<UUID> hubIds, Pageable pageable);

    Page<DeliveryManager> findAllByHubIdInAndTypeAndDeletedAtIsNull(Collection<UUID> hubIds, DeliveryManagerType type, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DeliveryManager d WHERE d.hubId = :hubId AND d.deletedAt IS NULL ORDER BY d.deliverySequence DESC")
    List<DeliveryManager> findAllByHubIdForUpdate(@Param("hubId") UUID hubId);
}
