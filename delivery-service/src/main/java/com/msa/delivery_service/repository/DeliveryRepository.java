package com.msa.delivery_service.repository;

import com.msa.delivery_service.entity.Delivery;
import com.msa.delivery_service.domain.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    boolean existsByOrderId(UUID orderId);

    Optional<Delivery> findByOrderId(UUID orderId);

    // ?뱀젙 ?낆껜 諛곗넚 ?대떦?먯뿉寃?諛곗젙??諛곗넚 紐⑸줉 議고쉶
    Page<Delivery> findAllByCompanyDeliveryManagerId(UUID companyDeliveryManagerId, Pageable pageable);

    // ?꾩쭅 諛곗넚 以묒씤 ?낆껜 諛곗넚 ?대떦??ID 紐⑸줉 議고쉶
    @Query("""
        select distinct d.companyDeliveryManagerId
        from Delivery d
        where d.companyDeliveryManagerId in :managerIds
            and d.deletedAt is null
            and d.status not in :finishedStatuses
    """)
    Set<UUID> findWorkingManagerIds(
            @Param("managerIds") Collection<UUID> managerIds,
            @Param("finishedStatuses") Collection<DeliveryStatus> finishedStatuses
    );
}
