package com.msa.delivery_service.infrastructure.repository;

import com.msa.delivery_service.domain.entity.Delivery;
import com.msa.delivery_service.domain.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    boolean existsByOrderId(UUID orderId);

    // 상태를 통해 배송 중인 기사들 조회
    @Query("""
        select distinct d.companyDeliveryManagerId 
        from Delivery d
        where d.companyDeliveryManagerId in :managerIds
            and d.status not in :finishedStatuses
""")
    Set<UUID> findWorkingManagerIds(
            @Param("managerIds") Collection<UUID> managerIds,
            @Param("finishedStatuses") Collection<DeliveryStatus> finishedStatuses
    );
}
