package com.msa.delivery_service.application;

import com.msa.core_common.error.exception.CustomException;
import com.msa.delivery_service.domain.entity.Delivery;
import com.msa.delivery_service.domain.entity.DeliveryRouteHistory;
import com.msa.delivery_service.domain.enums.DeliveryErrorCode;
import com.msa.delivery_service.domain.enums.DeliveryStatus;
import com.msa.delivery_service.infrastructure.client.hub.HubClient;
import com.msa.delivery_service.infrastructure.client.hub.dto.HubRouteResponse;
import com.msa.delivery_service.infrastructure.client.user.UserClient;
import com.msa.delivery_service.infrastructure.client.user.dto.DeliveryManagerResponse;
import com.msa.delivery_service.infrastructure.client.user.dto.HubManagerResponse;
import com.msa.delivery_service.infrastructure.repository.DeliveryRepository;
import com.msa.delivery_service.infrastructure.repository.DeliveryRouteHistoryRepository;
import com.msa.delivery_service.infrastructure.stream.RedisStreamEventPublisher;
import com.msa.delivery_service.infrastructure.stream.DeadlineRequestedEvent;
import com.msa.delivery_service.presentation.dto.DeliveryRequest;
import com.msa.delivery_service.presentation.dto.DeliveryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private static final String WORK_START_TIME = "09:00";
    private static final String WORK_END_TIME = "18:00";

    private final DeliveryRepository deliveryRepository;
    private final DeliveryRouteHistoryRepository deliveryRouteHistoryRepository;
    private final HubClient hubClient;
    private final UserClient userClient;
    private final RedisStreamEventPublisher redisStreamEventPublisher;

    @Transactional
    public DeliveryResponse createDelivery(DeliveryRequest request) {
        HubManagerResponse hubManager = userClient.getHubManager(request.getDepartureHubId());
        DeliveryManagerResponse deliveryManager = selectDeliveryManager(
                userClient.getDeliveryManagers(request.getDepartureHubId())
        );
        List<HubRouteResponse> hubRoutes = getHubRoutes(request);

        Delivery delivery = Delivery.create(
                request.getOrderId(),
                request.getDepartureHubId(),
                request.getDestinationHubId(),
                request.getReceiverCompanyId(),
                deliveryManager.getDeliveryManagerId(),
                request.getDeliveryAddress(),
                request.getReceiverName(),
                hubManager.getHubManagerSlackId()
        );
        Delivery savedDelivery = deliveryRepository.save(delivery);

        List<DeliveryRouteHistory> routeHistories = HubRouteResponse.toDeliveryRouteHistories(
                savedDelivery,
                deliveryManager.getDeliveryManagerId(),
                hubRoutes
        );
        deliveryRouteHistoryRepository.saveAll(routeHistories);

        redisStreamEventPublisher.publishAfterCommit(
                RedisStreamEventPublisher.DEADLINE_REQUESTED_STREAM,
                DeadlineRequestedEvent.of(
                        savedDelivery,
                        request,
                        hubManager,
                        deliveryManager,
                        hubRoutes,
                        WORK_START_TIME,
                        WORK_END_TIME
                )
        );

        return DeliveryResponse.from(savedDelivery);
    }

    private DeliveryManagerResponse selectDeliveryManager(List<DeliveryManagerResponse> deliveryManagers) {
        if (deliveryManagers == null || deliveryManagers.isEmpty()) {
            throw new CustomException(DeliveryErrorCode.NO_DELIVERY_MANAGER);
        }
        // 배송 중인 배송 담당자들 필터링
        Set<UUID> workingManagerIds = deliveryRepository.findWorkingManagerIds(
                deliveryManagers.stream()
                        .map(DeliveryManagerResponse::getDeliveryManagerId)
                        .toList(),
                List.of(DeliveryStatus.DELIVERED, DeliveryStatus.CANCELLED)
        );
        // 배송 가능한 배송 담당자들 필터링
        List<DeliveryManagerResponse> availableManagers = deliveryManagers.stream()
                .filter(deliveryManager -> !workingManagerIds.contains(deliveryManager.getDeliveryManagerId()))
                .toList();

        if (availableManagers.isEmpty()) {
            throw new CustomException(DeliveryErrorCode.NO_DELIVERY_MANAGER);
        }
        // 가장 빠른 순번 반환
        return Collections.min(
                availableManagers,
                Comparator.comparing(DeliveryManagerResponse::getDeliverySequence)
        );
    }

    private List<HubRouteResponse> getHubRoutes(DeliveryRequest request) {
        List<HubRouteResponse> hubRoutes = hubClient.getRoutes(
                request.getDepartureHubId(),
                request.getDestinationHubId()
        );

        if (hubRoutes == null || hubRoutes.isEmpty()) {
            throw new CustomException(DeliveryErrorCode.NO_HUB_ROUTE);
        }
        return hubRoutes;
    }
}
