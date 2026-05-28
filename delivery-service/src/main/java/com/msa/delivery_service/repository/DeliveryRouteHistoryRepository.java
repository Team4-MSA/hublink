package com.msa.delivery_service.repository;

import com.msa.delivery_service.entity.DeliveryRouteHistory;
import com.msa.delivery_service.domain.enums.DeliveryRouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface DeliveryRouteHistoryRepository extends JpaRepository<DeliveryRouteHistory, UUID> {

    // ?뱀젙 諛곗넚???랁븳 寃쎈줈 ?대젰???쒕쾲 ?ㅻ쫫李⑥닚?쇰줈 議고쉶
    List<DeliveryRouteHistory> findByDeliveryDeliveryIdOrderBySequenceAsc(UUID deliveryId);

    // ?뱀젙 諛곗넚???뱀젙 諛곗넚 ?대떦?먭? 諛곗젙??寃쎈줈 ?대젰???덈뒗吏 ?뺤씤
    boolean existsByDeliveryDeliveryIdAndDeliveryManagerId(UUID deliveryId, UUID deliveryManagerId);

    // ?꾩쭅 諛곗넚 以묒씤 ?덈툕 諛곗넚 ?대떦??ID 紐⑸줉??議고쉶
    @Query("""
        select distinct rh.deliveryManagerId
        from DeliveryRouteHistory rh
        where rh.deliveryManagerId in :managerIds
            and rh.deletedAt is null
            and rh.status not in :finishedStatuses
    """)
    Set<UUID> findWorkingManagerIds(
            @Param("managerIds") Collection<UUID> managerIds,
            @Param("finishedStatuses") Collection<DeliveryRouteStatus> finishedStatuses
    );
}
