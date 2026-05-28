package com.msa.delivery_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.delivery_service.entity.Delivery;
import com.msa.delivery_service.entity.DeliveryRouteHistory;
import com.msa.delivery_service.domain.enums.DeliveryErrorCode;
import com.msa.delivery_service.domain.enums.DeliveryRouteStatus;
import com.msa.delivery_service.domain.enums.DeliveryStatus;
import com.msa.delivery_service.client.hub.HubClient;
import com.msa.delivery_service.dto.HubRouteResponse;
import com.msa.delivery_service.client.user.UserClient;
import com.msa.delivery_service.dto.DeliveryManagerResponse;
import com.msa.delivery_service.dto.HubManagerResponse;
import com.msa.delivery_service.repository.DeliveryRepository;
import com.msa.delivery_service.repository.DeliveryRouteHistoryRepository;
import com.msa.delivery_service.message.DeadlineGeneratedEvent;
import com.msa.delivery_service.dto.DeliveryDetailResponse;
import com.msa.delivery_service.dto.DeliveryRequest;
import com.msa.delivery_service.dto.DeliveryResponse;
import com.msa.delivery_service.dto.DeliveryRouteHistoryResponse;
import com.msa.delivery_service.dto.DeliveryRouteStatusUpdateRequest;
import com.msa.delivery_service.dto.DeliveryStatusUpdateRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
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

    // 諛곗넚 ?대떦??洹쇰Т ?쒓컙 怨좎젙
    private static final String WORK_START_TIME = "09:00";
    private static final String WORK_END_TIME = "18:00";

    // 諛곗넚 ?대떦?????援щ텇
    private static final String COMPANY_DELIVERY_MANAGER_TYPE = "COMPANY_DELIVERY";
    private static final String HUB_DELIVERY_MANAGER_TYPE = "HUB_DELIVERY";

    // USER 沅뚰븳
    private static final String MASTER = "MASTER";
    private static final String HUB_MANAGER = "HUB_MANAGER";
    private static final String DELIVERY_MANAGER = "DELIVERY_MANAGER";
    private static final String SUPPLIER_MANAGER = "SUPPLIER_MANAGER";

    private final DeliveryRepository deliveryRepository;
    private final DeliveryRouteHistoryRepository deliveryRouteHistoryRepository;
    private final HubClient hubClient;
    private final UserClient userClient;
    private final DeliveryCreateService deliveryCreateService;
    private final DeliveryAssignmentLockService deliveryAssignmentLockService;

    @Transactional(readOnly = true)
    public PageRes<DeliveryResponse> getDeliveries(String role, Pageable pageable) {
        // MASTER: ?꾩껜 諛곗넚 紐⑸줉 議고쉶 媛??
        // 洹???沅뚰븳: ?묎렐 遺덇?
        Page<DeliveryResponse> deliveries = switch (role) {
            case MASTER -> deliveryRepository.findAll(pageable)
                    .map(DeliveryResponse::from);
            default -> throw new CustomException(DeliveryErrorCode.ACCESS_DENIED);
        };

        return new PageRes<>(deliveries);
    }

    @Transactional(readOnly = true)
    public PageRes<DeliveryResponse> getMyDeliveries(UUID userId, String role, Pageable pageable) {
        // DELIVERY_MANAGER: 蹂몄씤?먭쾶 諛곗젙??諛곗넚 紐⑸줉 議고쉶 媛??
        // 洹???沅뚰븳: ?묎렐 遺덇?
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

        // MASTER, HUB_MANAGER, SUPPLIER_MANAGER: 諛곗넚 ?곸꽭 議고쉶 媛??
        // DELIVERY_MANAGER: 蹂몄씤?먭쾶 諛곗젙??諛곗넚留?議고쉶 媛??(寃쎈줈瑜??ы븿?섍린 ?뚮Ц???덈툕 諛곗넚 ?대떦?먮룄 議고쉶 媛??
        return switch (role) {
            case MASTER, HUB_MANAGER, SUPPLIER_MANAGER -> DeliveryDetailResponse.of(delivery, routeHistories);
            case DELIVERY_MANAGER -> {
                boolean assignedDelivery = userId.equals(delivery.getCompanyDeliveryManagerId())
                        || deliveryRouteHistoryRepository.existsByDeliveryDeliveryIdAndDeliveryManagerId(
                        delivery.getDeliveryId(),
                        userId
                );
                if (!assignedDelivery) throw new CustomException(DeliveryErrorCode.ACCESS_DENIED);

                yield DeliveryDetailResponse.of(delivery, routeHistories);
            }
            default -> throw new CustomException(DeliveryErrorCode.ACCESS_DENIED);
        };
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryByOrderId(String role, UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_NOT_FOUND));

        // MASTER, HUB_MANAGER, SUPPLIER_MANAGER: 二쇰Ц 湲곗? 諛곗넚 議고쉶 媛??
        // 洹???沅뚰븳: ?묎렐 遺덇?
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

        // MASTER, HUB_MANAGER: ???諛곗넚 ?곹깭 蹂寃?媛??
        // DELIVERY_MANAGER: ?낆껜 諛곗넚 ?대떦?먯씤 寃쎌슦留?蹂寃?媛??
        // 洹???沅뚰븳: ?묎렐 遺덇?
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

        deliveryRepository.flush();

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

        // MASTER, HUB_MANAGER: 寃쎈줈 ?곹깭 蹂寃?媛??
        // DELIVERY_MANAGER: 蹂몄씤?먭쾶 諛곗젙??寃쎈줈留?蹂寃?媛??
        // 洹???沅뚰븳: ?묎렐 遺덇?
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

        deliveryRouteHistoryRepository.flush();

        return DeliveryRouteHistoryResponse.from(routeHistory);
    }

    /*
        ?대? ?몄텧 API
    */

    public DeliveryResponse createDelivery(DeliveryRequest request) {
        if (deliveryRepository.existsByOrderId(request.getOrderId())) {
            throw new CustomException(DeliveryErrorCode.DUPLICATE_ORDER_DELIVERY);
        }

        List<HubRouteResponse> hubRoutes = getHubRoutes(request);
        HubManagerResponse hubManager = getHubManager(getDepartureHubId(hubRoutes));
        List<DeliveryManagerResponse> deliveryManagers = getDeliveryManagers(hubRoutes);
        // ?낆껜 諛곗넚 湲곗궗 Lock ??1媛?+ ?덈툕 諛곗넚 湲곗궗 Lock ??N媛?
        List<String> lockKeys = buildAssignmentLockKeys(hubRoutes);

        // Lock???꾨? ?↔퀬 ?몄옄濡??ㅼ뼱媛?function???섑뻾
        return deliveryAssignmentLockService.executeWithLocks(lockKeys, () -> {
            DeliveryManagerResponse companyDeliveryManager = assignCompanyDeliveryManager(
                    deliveryManagers,
                    getDestinationHubId(hubRoutes)
            );
            Map<UUID, UUID> hubDeliveryManagerIds = assignHubDeliveryManagers(hubRoutes, deliveryManagers);

            return deliveryCreateService.createDelivery(
                    request,
                    hubManager,
                    companyDeliveryManager,
                    hubRoutes,
                    hubDeliveryManagerIds,
                    WORK_START_TIME,
                    WORK_END_TIME
            );
        });
    }

    private List<String> buildAssignmentLockKeys(List<HubRouteResponse> hubRoutes) {
        List<String> lockKeys = new ArrayList<>();

        // 留덉?留??덈툕媛 ?낆껜 諛곗넚 ?덈툕?대?濡??곕줈 Lock ???앹꽦
        UUID destinationHubId = getDestinationHubId(hubRoutes);
        lockKeys.add("lock:delivery:company:" + destinationHubId);

        // 留덉?留?寃쎈줈瑜??쒖쇅?섍퀬???꾨? ?덈툕-?덈툕 寃쎈줈
        for (int i = 0; i < hubRoutes.size() - 1; i++) {
            lockKeys.add("lock:delivery:hub:" + hubRoutes.get(i).getDepartureHubId());
        }
        return lockKeys;
    }

    @Transactional
    public void updateFinalDepartureDeadline(DeadlineGeneratedEvent event) {
        Delivery delivery = deliveryRepository.findById(event.getDeliveryId())
                .orElseThrow(() -> new CustomException(DeliveryErrorCode.DELIVERY_NOT_FOUND));
        delivery.updateFinalDepartureDeadline(event.getFinalDepartureDeadline());
        deliveryRepository.flush();
    }

    @Transactional
    public void compensateDeliveryCreation(UUID orderId) {
        deliveryRepository.findByOrderId(orderId)
                .ifPresent(delivery -> {
                    delivery.cancel();
                    delivery.delete("SYSTEM");
                    deliveryRouteHistoryRepository.findByDeliveryDeliveryIdOrderBySequenceAsc(delivery.getDeliveryId())
                        .forEach(routeHistory -> {
                            if (routeHistory.getStatus().canChangeTo(DeliveryRouteStatus.FAILED)) {
                                routeHistory.updateStatus(DeliveryRouteStatus.FAILED);
                            }
                            routeHistory.delete("SYSTEM");
                        });
                    deliveryRouteHistoryRepository.flush();
                    deliveryRepository.flush();
                });
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

    // 諛곗넚 寃쎈줈???꾩슂???덈툕?ㅼ쓽 諛곗넚 ?대떦??紐⑸줉 議고쉶
    private List<DeliveryManagerResponse> getDeliveryManagers(List<HubRouteResponse> hubRoutes) {
        Set<UUID> hubIds = new LinkedHashSet<>();
        for (HubRouteResponse hubRoute : hubRoutes) {
            hubIds.add(hubRoute.getDepartureHubId());
        }

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

    // 留덉?留??낆껜 諛곗넚???대떦??諛곗넚 ?대떦??諛곗젙
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

    // ?덈툕 媛??대룞 寃쎈줈留덈떎 ?덈툕 諛곗넚 ?대떦??諛곗젙 - <?덈툕 ID, 諛곗넚 ?대떦??ID> 諛섑솚
    private Map<UUID, UUID> assignHubDeliveryManagers(
            List<HubRouteResponse> hubRoutes,
            List<DeliveryManagerResponse> deliveryManagers
    ) {
        Map<UUID, UUID> hubDeliveryManagerIds = new HashMap<>();
        // 媛??덈툕???대떦 ?섎뒗 ?대떦 留ㅻ땲?瑜??곕줈 議고쉶?댁꽌 諛쒖깮?섎뜕 N+1 臾몄젣 諛⑹?
        // 泥섏쓬遺??紐⑤뱺 寃쎈줈???덈툕 諛곗넚 ?대떦?먮뱾???꾨? 誘몃━ 議고쉶
        List<DeliveryManagerResponse> hubDeliveryManagers = deliveryManagers.stream()
                .filter(deliveryManager -> HUB_DELIVERY_MANAGER_TYPE.equals(deliveryManager.getType()))
                .toList();
        Set<UUID> workingManagerIds = hubDeliveryManagers.isEmpty()
                ? Set.of()
                : deliveryRouteHistoryRepository.findWorkingManagerIds(
                hubDeliveryManagers.stream()
                        .map(DeliveryManagerResponse::getDeliveryManagerId)
                        .toList(),
                List.of(DeliveryRouteStatus.COMPLETED, DeliveryRouteStatus.SKIPPED, DeliveryRouteStatus.FAILED)
        );

        for (int i = 0; i < hubRoutes.size() - 1; i++) {
            HubRouteResponse hubRoute = hubRoutes.get(i);
            DeliveryManagerResponse hubDeliveryManager = selectHubDeliveryManager(
                    deliveryManagers,
                    hubRoute.getDepartureHubId(),
                    workingManagerIds
            );
            hubDeliveryManagerIds.put(hubRoute.getHubRouteId(), hubDeliveryManager.getDeliveryManagerId());
        }

        return hubDeliveryManagerIds;
    }

    // ?뱀젙 異쒕컻 ?덈툕 援ш컙???대떦???덈툕 諛곗넚 ?대떦???좏깮
    private DeliveryManagerResponse selectHubDeliveryManager(
            List<DeliveryManagerResponse> deliveryManagers,
            UUID departureHubId,
            Set<UUID> workingManagerIds
    ) {
        List<DeliveryManagerResponse> hubDeliveryManagers = deliveryManagers.stream()
                .filter(deliveryManager -> departureHubId.equals(deliveryManager.getHubId()))
                .filter(deliveryManager -> HUB_DELIVERY_MANAGER_TYPE.equals(deliveryManager.getType()))
                .toList();

        if (hubDeliveryManagers.isEmpty()) {
            throw new CustomException(DeliveryErrorCode.NO_DELIVERY_MANAGER);
        }

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

    // 異쒕컻 ?덈툕? ?꾩갑 ?덈툕 湲곗??쇰줈 諛곗넚 寃쎈줈 議고쉶
    private List<HubRouteResponse> getHubRoutes(DeliveryRequest request) {
        try {
            List<HubRouteResponse> hubRoutes = hubClient.getRoutes(
                    request.getSupplyCompanyId(),
                    request.getReceiverCompanyId()
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

    private UUID getDepartureHubId(List<HubRouteResponse> hubRoutes) {
        return hubRoutes.get(0).getDepartureHubId();
    }

    // Hub-Hub 寃쎈줈媛 ?꾨땶 ?먯냼??寃쎌슦 Hub-Company?대?濡??대떦 寃쎈줈??異쒕컻 hub媛 留덉?留?hub
    private UUID getDestinationHubId(List<HubRouteResponse> hubRoutes) {
        return hubRoutes.get(hubRoutes.size() - 1).getDepartureHubId();
    }
}
