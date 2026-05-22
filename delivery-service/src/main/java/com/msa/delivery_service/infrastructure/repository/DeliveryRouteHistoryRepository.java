package com.msa.delivery_service.infrastructure.repository;

import com.msa.delivery_service.domain.entity.DeliveryRouteHistory;
import com.msa.delivery_service.domain.enums.DeliveryRouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface DeliveryRouteHistoryRepository extends JpaRepository<DeliveryRouteHistory, UUID> {

    @Query("""
        select distinct rh.deliveryManagerId
        from DeliveryRouteHistory rh
        where rh.deliveryManagerId in :managerIds
            and rh.status not in :finishedStatuses
""")
    Set<UUID> findWorkingManagerIds(
            @Param("managerIds") Collection<UUID> managerIds,
            @Param("finishedStatuses") Collection<DeliveryRouteStatus> finishedStatuses
    );
}
