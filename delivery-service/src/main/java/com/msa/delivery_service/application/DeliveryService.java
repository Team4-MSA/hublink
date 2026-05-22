package com.msa.delivery_service.application;

import com.msa.core_common.error.exception.CustomException;
import com.msa.delivery_service.domain.entity.Delivery;
import com.msa.delivery_service.domain.entity.DeliveryRouteHistory;
import com.msa.delivery_service.domain.enums.DeliveryErrorCode;
import com.msa.delivery_service.domain.enums.DeliveryRouteStatus;
import com.msa.delivery_service.domain.enums.DeliveryStatus;
import com.msa.delivery_service.infrastructure.client.hub.HubClient;
import com.msa.delivery_service.infrastructure.client.hub.dto.HubRouteResponse;
import com.msa.delivery_service.infrastructure.client.user.UserClient;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private static final String WORK_START_TIME = "09:00";
    private static final String WORK_END_TIME = "18:00";
    private static final String COMPANY_DELIVERY_MANAGER_TYPE = "COMPANY_DELIVERY";
    private static final String HUB_DELIVERY_MANAGER_TYPE = "HUB_DELIVERY";

    private final DeliveryRepository deliveryRepository;
    private final DeliveryRouteHistoryRepository deliveryRouteHistoryRepository;
    private final HubClient hubClient;
    private final UserClient userClient;
    private final RedisStreamEventPublisher redisStreamEventPublisher;

    @Transactional
    public DeliveryResponse createDelivery(DeliveryRequest request) {
        if (deliveryRepository.existsByOrderId(request.getOrderId())) {
            throw new CustomException(DeliveryErrorCode.DUPLICATE_ORDER_DELIVERY);
        }

        HubManagerResponse hubManager = userClient.getHubManager(request.getDepartureHubId());
        List<HubRouteResponse> hubRoutes = getHubRoutes(request);
        List<DeliveryManagerResponse> deliveryManagers = getDeliveryManagers(request, hubRoutes);

        // 배송 테이블에 들어갈 업체 배송 담당자 배정
        DeliveryManagerResponse companyDeliveryManager = assignCompanyDeliveryManager(
                deliveryManagers,
                request.getDestinationHubId()
        );
        // 배송 경로 테이블에 들어갈 허브 배송 담당자들 배정
        Map<UUID, UUID> hubDeliveryManagerIds = assignHubDeliveryManagers(hubRoutes, deliveryManagers);

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
                        WORK_START_TIME,
                        WORK_END_TIME
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

    private List<DeliveryManagerResponse> getDeliveryManagers(
            DeliveryRequest request,
            List<HubRouteResponse> hubRoutes
    ) {
        Set<UUID> hubIds = new LinkedHashSet<>();
        for (HubRouteResponse hubRoute : hubRoutes) {
            hubIds.add(hubRoute.getDepartureHubId());
        }
        hubIds.add(request.getDestinationHubId());

        List<DeliveryManagerResponse> deliveryManagers = userClient.getDeliveryManagers(new ArrayList<>(hubIds));
        if (deliveryManagers == null || deliveryManagers.isEmpty()) {
            throw new CustomException(DeliveryErrorCode.NO_DELIVERY_MANAGER);
        }

        return deliveryManagers;
    }

    private DeliveryManagerResponse assignCompanyDeliveryManager(
            List<DeliveryManagerResponse> deliveryManagers,
            UUID destinationHubId
    ) {
        List<DeliveryManagerResponse> companyDeliveryManagers = deliveryManagers.stream()
                .filter(deliveryManager -> destinationHubId.equals(deliveryManager.getHubId()))
                .filter(deliveryManager -> COMPANY_DELIVERY_MANAGER_TYPE.equals(deliveryManager.getType()))
                .toList();

        if (companyDeliveryManagers.isEmpty()) {
            throw new CustomException(DeliveryErrorCode.NO_DELIVERY_MANAGER);
        }

        Set<UUID> workingManagerIds = deliveryRepository.findWorkingManagerIds(
                companyDeliveryManagers.stream()
                        .map(DeliveryManagerResponse::getDeliveryManagerId)
                        .toList(),
                List.of(DeliveryStatus.DELIVERED, DeliveryStatus.CANCELLED)
        );

        List<DeliveryManagerResponse> availableManagers = companyDeliveryManagers.stream()
                .filter(deliveryManager -> !workingManagerIds.contains(deliveryManager.getDeliveryManagerId()))
                .toList();

        if (availableManagers.isEmpty()) {
            throw new CustomException(DeliveryErrorCode.NO_DELIVERY_MANAGER);
        }

        return Collections.min(
                availableManagers,
                Comparator.comparing(DeliveryManagerResponse::getDeliverySequence)
        );
    }

    private Map<UUID, UUID> assignHubDeliveryManagers(
            List<HubRouteResponse> hubRoutes,
            List<DeliveryManagerResponse> deliveryManagers
    ) {
        Map<UUID, UUID> hubDeliveryManagerIds = new HashMap<>();

        for (HubRouteResponse hubRoute : hubRoutes) {
            DeliveryManagerResponse hubDeliveryManager = selectHubDeliveryManager(
                    deliveryManagers,
                    hubRoute.getDepartureHubId()
            );
            hubDeliveryManagerIds.put(hubRoute.getHubRouteId(), hubDeliveryManager.getDeliveryManagerId());
        }

        return hubDeliveryManagerIds;
    }

    private DeliveryManagerResponse selectHubDeliveryManager(
            List<DeliveryManagerResponse> deliveryManagers,
            UUID departureHubId
    ) {
        List<DeliveryManagerResponse> hubDeliveryManagers = deliveryManagers.stream()
                .filter(deliveryManager -> departureHubId.equals(deliveryManager.getHubId()))
                .filter(deliveryManager -> HUB_DELIVERY_MANAGER_TYPE.equals(deliveryManager.getType()))
                .toList();

        if (hubDeliveryManagers.isEmpty()) {
            throw new CustomException(DeliveryErrorCode.NO_DELIVERY_MANAGER);
        }

        Set<UUID> workingManagerIds = deliveryRouteHistoryRepository.findWorkingManagerIds(
                hubDeliveryManagers.stream()
                        .map(DeliveryManagerResponse::getDeliveryManagerId)
                        .toList(),
                List.of(DeliveryRouteStatus.COMPLETED, DeliveryRouteStatus.SKIPPED, DeliveryRouteStatus.FAILED)
        );

        List<DeliveryManagerResponse> availableManagers = hubDeliveryManagers.stream()
                .filter(deliveryManager -> !workingManagerIds.contains(deliveryManager.getDeliveryManagerId()))
                .toList();

        if (availableManagers.isEmpty()) {
            throw new CustomException(DeliveryErrorCode.NO_DELIVERY_MANAGER);
        }

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
