package com.msa.delivery_service.application;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
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
import com.msa.delivery_service.infrastructure.stream.DeadlineGeneratedEvent;
import com.msa.delivery_service.infrastructure.stream.DeadlineRequestedEvent;
import com.msa.delivery_service.infrastructure.stream.RedisStreamEventPublisher;
import com.msa.delivery_service.presentation.dto.DeliveryDetailResponse;
import com.msa.delivery_service.presentation.dto.DeliveryRequest;
import com.msa.delivery_service.presentation.dto.DeliveryResponse;
import com.msa.delivery_service.presentation.dto.DeliveryRouteHistoryResponse;
import com.msa.delivery_service.presentation.dto.DeliveryRouteStatusUpdateRequest;
import com.msa.delivery_service.presentation.dto.DeliveryStatusUpdateRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // 배송 담당자 근무 시간 고정
    private static final String WORK_START_TIME = "09:00";
    private static final String WORK_END_TIME = "18:00";

    // 배송 담당자 타입 구분
    private static final String COMPANY_DELIVERY_MANAGER_TYPE = "COMPANY_DELIVERY";
    private static final String HUB_DELIVERY_MANAGER_TYPE = "HUB_DELIVERY";

    // USER 권한
    private static final String MASTER = "MASTER";
    private static final String HUB_MANAGER = "HUB_MANAGER";
    private static final String DELIVERY_MANAGER = "DELIVERY_MANAGER";
    private static final String SUPPLIER_MANAGER = "SUPPLIER_MANAGER";

    private final DeliveryRepository deliveryRepository;
    private final DeliveryRouteHistoryRepository deliveryRouteHistoryRepository;
    private final HubClient hubClient;
    private final UserClient userClient;
    private final RedisStreamEventPublisher redisStreamEventPublisher;

    @Transactional(readOnly = true)
    public PageRes<DeliveryResponse> getDeliveries(String role, Pageable pageable) {
        // MASTER: 전체 배송 목록 조회 가능
        // 그 외 권한: 접근 불가
        Page<DeliveryResponse> deliveries = switch (role) {
            case MASTER -> deliveryRepository.findAll(pageable)
                    .map(DeliveryResponse::from);
            default -> throw new CustomException(DeliveryErrorCode.ACCESS_DENIED);
        };

        return new PageRes<>(deliveries);
    }

    @Transactional(readOnly = true)
    public PageRes<DeliveryResponse> getMyDeliveries(UUID userId, String role, Pageable pageable) {
        // DELIVERY_MANAGER: 본인에게 배정된 배송 목록 조회 가능
        // 그 외 권한: 접근 불가
        Page<DeliveryResponse> deliveries = switch (role) {
            case DELIVERY_MANAGER -> deliveryRepository.findAllByCompanyDeliveryManagerId(userId, pageable)
                    .map(DeliveryResponse::from);
            default -> throw new CustomException(DeliveryErrorCode.ACCESS_DENIED);
        };

        return new PageRes<>(deliveries);
    }

    @Transactional(readOnly = true)
    public DeliveryDetailResponse getDelivery(UUID userId, String role, UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_NOT_FOUND));
        List<DeliveryRouteHistory> routeHistories = deliveryRouteHistoryRepository
                .findByDeliveryDeliveryIdOrderBySequenceAsc(deliveryId);

        // MASTER, HUB_MANAGER, SUPPLIER_MANAGER: 배송 상세 조회 가능
        // DELIVERY_MANAGER: 본인에게 배정된 배송만 조회 가능 (경로를 포함하기 때문에 허브 배송 담당자도 조회 가능)
        return switch (role) {
            case MASTER, HUB_MANAGER, SUPPLIER_MANAGER -> DeliveryDetailResponse.of(delivery, routeHistories);
            case DELIVERY_MANAGER -> {
                boolean assignedDelivery = userId.equals(delivery.getCompanyDeliveryManagerId())
                        || deliveryRouteHistoryRepository.existsByDeliveryDeliveryIdAndDeliveryManagerId(
                        delivery.getDeliveryId(),
                        userId
                );
                if (!assignedDelivery) {
                    throw new CustomException(DeliveryErrorCode.ACCESS_DENIED);
                }
                yield DeliveryDetailResponse.of(delivery, routeHistories);
            }
            default -> throw new CustomException(DeliveryErrorCode.ACCESS_DENIED);
        };
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryByOrderId(String role, UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_NOT_FOUND));

        // MASTER, HUB_MANAGER, SUPPLIER_MANAGER: 주문 기준 배송 조회 가능
        // 그 외 권한: 접근 불가
        return switch (role) {
            case MASTER, HUB_MANAGER, SUPPLIER_MANAGER -> DeliveryResponse.from(delivery);
            default -> throw new CustomException(DeliveryErrorCode.ACCESS_DENIED);
        };
    }

    @Transactional
    public DeliveryResponse updateDeliveryStatus(
            UUID userId,
            String role,
            UUID deliveryId,
            DeliveryStatusUpdateRequest request
    ) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_NOT_FOUND));

        // MASTER, HUB_MANAGER: 대표 배송 상태 변경 가능
        // DELIVERY_MANAGER: 업체 배송 담당자인 경우만 변경 가능
        // 그 외 권한: 접근 불가
        switch (role) {
            case MASTER, HUB_MANAGER -> {}
            case DELIVERY_MANAGER -> {
                if (!userId.equals(delivery.getCompanyDeliveryManagerId())) {
                    throw new CustomException(DeliveryErrorCode.ACCESS_DENIED);
                }
            }
            default -> throw new CustomException(DeliveryErrorCode.ACCESS_DENIED);
        }

        if (request.getStatus() == DeliveryStatus.DELIVERED) {
            delivery.complete();
        } else {
            delivery.updateStatus(request.getStatus());
        }

        return DeliveryResponse.from(delivery);
    }

    @Transactional
    public DeliveryRouteHistoryResponse updateRouteHistoryStatus(
            UUID userId,
            String role,
            UUID routeHistoryId,
            DeliveryRouteStatusUpdateRequest request
    ) {
        DeliveryRouteHistory routeHistory = deliveryRouteHistoryRepository.findById(routeHistoryId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_ROUTE_HISTORY_NOT_FOUND));

        // MASTER, HUB_MANAGER: 경로 상태 변경 가능
        // DELIVERY_MANAGER: 본인에게 배정된 경로만 변경 가능
        // 그 외 권한: 접근 불가
        switch (role) {
            case MASTER, HUB_MANAGER -> {}
            case DELIVERY_MANAGER -> {
                if (!userId.equals(routeHistory.getDeliveryManagerId())) {
                    throw new CustomException(DeliveryErrorCode.ACCESS_DENIED);
                }
            }
            default -> throw new CustomException(DeliveryErrorCode.ACCESS_DENIED);
        }

        if (request.getStatus() == DeliveryRouteStatus.COMPLETED) {
            routeHistory.complete(request.getActualDistanceKm(), request.getActualDurationMin());
        } else {
            routeHistory.updateStatus(request.getStatus());
        }

        if (request.getStatusMessage() != null) {
            routeHistory.updateStatusMessage(request.getStatusMessage());
        }

        return DeliveryRouteHistoryResponse.from(routeHistory);
    }

    /*
        내부 호출 API
    */

    @Transactional
    public DeliveryResponse createDelivery(DeliveryRequest request) {
        if (deliveryRepository.existsByOrderId(request.getOrderId())) {
            throw new CustomException(DeliveryErrorCode.DUPLICATE_ORDER_DELIVERY);
        }

        HubManagerResponse hubManager = getHubManager(request.getDepartureHubId());
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

    @Transactional
    public void updateFinalDepartureDeadline(DeadlineGeneratedEvent event) {
        Delivery delivery = deliveryRepository.findById(event.getDeliveryId())
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_NOT_FOUND));

        delivery.updateFinalDepartureDeadline(event.getFinalDepartureDeadline());
    }

    // 배송 저장 시 중복 주문 예외 처리
    private Delivery saveDelivery(Delivery delivery) {
        try {
            return deliveryRepository.saveAndFlush(delivery);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(DeliveryErrorCode.DUPLICATE_ORDER_DELIVERY);
        }
    }

    private HubManagerResponse getHubManager(UUID departureHubId) {
        try {
            HubManagerResponse hubManager = userClient.getHubManager(departureHubId);

            if (hubManager == null || hubManager.getHubManagerSlackId() == null) {
                throw new CustomException(DeliveryErrorCode.NO_HUB_MANAGER);
            }

            return hubManager;
        } catch (FeignException.NotFound e) {
            throw new CustomException(DeliveryErrorCode.NO_HUB_MANAGER);
        } catch (FeignException e) {
            throw new CustomException(DeliveryErrorCode.USER_SERVICE_UNAVAILABLE);
        }
    }

    // 배송 경로에 필요한 허브들의 배송 담당자 목록 조회
    private List<DeliveryManagerResponse> getDeliveryManagers(
            DeliveryRequest request,
            List<HubRouteResponse> hubRoutes
    ) {
        Set<UUID> hubIds = new LinkedHashSet<>();
        for (HubRouteResponse hubRoute : hubRoutes) {
            hubIds.add(hubRoute.getDepartureHubId());
        }
        hubIds.add(request.getDestinationHubId());

        try {
            List<DeliveryManagerResponse> deliveryManagers = userClient.getDeliveryManagers(new ArrayList<>(hubIds));
            if (deliveryManagers == null || deliveryManagers.isEmpty()) {
                throw new CustomException(DeliveryErrorCode.NO_DELIVERY_MANAGER);
            }
            return deliveryManagers;
        } catch (FeignException.NotFound e) {
            throw new CustomException(DeliveryErrorCode.NO_DELIVERY_MANAGER);
        } catch (FeignException e) {
            throw new CustomException(DeliveryErrorCode.USER_SERVICE_UNAVAILABLE);
        }
    }

    // 마지막 업체 배송을 담당할 배송 담당자 배정
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

    // 허브 간 이동 경로마다 허브 배송 담당자 배정 - <허브 ID, 배송 담당자 ID> 반환
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

    // 특정 출발 허브 구간을 담당할 허브 배송 담당자 선택
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

    // 출발 허브와 도착 허브 기준으로 배송 경로 조회
    private List<HubRouteResponse> getHubRoutes(DeliveryRequest request) {
        try {
            List<HubRouteResponse> hubRoutes = hubClient.getRoutes(
                    request.getDepartureHubId(),
                    request.getDestinationHubId()
            );
            if (hubRoutes == null || hubRoutes.isEmpty()) {
                throw new CustomException(DeliveryErrorCode.NO_HUB_ROUTE);
            }
            return hubRoutes;
        } catch (FeignException.NotFound e) {
            throw new CustomException(DeliveryErrorCode.NO_HUB_ROUTE);
        } catch (FeignException e) {
            throw new CustomException(DeliveryErrorCode.HUB_SERVICE_UNAVAILABLE);
        }
    }

}
