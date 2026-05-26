package com.msa.delivery_service.application;

import com.msa.core_common.error.exception.CustomException;
import com.msa.delivery_service.domain.entity.Delivery;
import com.msa.delivery_service.domain.entity.DeliveryRouteHistory;
import com.msa.delivery_service.domain.enums.DeliveryErrorCode;
import com.msa.delivery_service.infrastructure.client.hub.dto.HubRouteResponse;
import com.msa.delivery_service.infrastructure.client.user.dto.DeliveryManagerResponse;
import com.msa.delivery_service.infrastructure.client.user.dto.HubManagerResponse;
import com.msa.delivery_service.infrastructure.repository.DeliveryRepository;
import com.msa.delivery_service.infrastructure.repository.DeliveryRouteHistoryRepository;
import com.msa.delivery_service.infrastructure.stream.DeadlineRequestedEvent;
import com.msa.delivery_service.infrastructure.stream.RedisStreamEventPublisher;
import com.msa.delivery_service.presentation.dto.DeliveryRequest;
import com.msa.delivery_service.presentation.dto.DeliveryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryCreationTransactionalService {
    /*
        외부 서비스 호출과 DB 커넥션을 분리하기 위해 트랜잭션을 외부로 분리
        Self-Invocation 방지를 위해 따로 클래스 작성
    */
    private final DeliveryRepository deliveryRepository;
    private final DeliveryRouteHistoryRepository deliveryRouteHistoryRepository;
    private final RedisStreamEventPublisher redisStreamEventPublisher;

    @Transactional
    public DeliveryResponse createDelivery(
            DeliveryRequest request,
            HubManagerResponse hubManager,
            DeliveryManagerResponse companyDeliveryManager,
            List<HubRouteResponse> hubRoutes,
            Map<UUID, UUID> hubDeliveryManagerIds,
            String workStartTime,
            String workEndTime
    ) {
        Delivery delivery = Delivery.create(
                request.getOrderId(),
                request.getDepartureHubId(),
                request.getDestinationHubId(),
                request.getReceiverCompanyId(),
                companyDeliveryManager.getDeliveryManagerId(),
                request.getDeliveryAddress(),
                request.getReceiverName(),
                hubManager.getHubManagerSlackId()
        );
        Delivery savedDelivery = saveDelivery(delivery);

        List<DeliveryRouteHistory> routeHistories = HubRouteResponse.toDeliveryRouteHistories(
                savedDelivery,
                companyDeliveryManager.getDeliveryManagerId(),
                hubRoutes,
                hubDeliveryManagerIds
        );
        deliveryRouteHistoryRepository.saveAll(routeHistories);

        // 커밋이 완료되면 콜백으로 이벤트 발행
        redisStreamEventPublisher.publishAfterCommit(
                RedisStreamEventPublisher.DEADLINE_REQUESTED_STREAM,
                DeadlineRequestedEvent.of(
                        savedDelivery,
                        request,
                        hubManager,
                        companyDeliveryManager,
                        hubRoutes,
                        workStartTime,
                        workEndTime
                )
        );

        return DeliveryResponse.from(savedDelivery);
    }

    private Delivery saveDelivery(Delivery delivery) {
        try {
            return deliveryRepository.saveAndFlush(delivery);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(DeliveryErrorCode.DUPLICATE_ORDER_DELIVERY);
        }
    }
}
