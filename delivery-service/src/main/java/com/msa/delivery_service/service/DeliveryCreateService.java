package com.msa.delivery_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.delivery_service.entity.Delivery;
import com.msa.delivery_service.entity.DeliveryRouteHistory;
import com.msa.delivery_service.domain.enums.DeliveryErrorCode;
import com.msa.delivery_service.dto.HubRouteResponse;
import com.msa.delivery_service.dto.DeliveryManagerResponse;
import com.msa.delivery_service.dto.HubManagerResponse;
import com.msa.delivery_service.repository.DeliveryRepository;
import com.msa.delivery_service.repository.DeliveryRouteHistoryRepository;
import com.msa.delivery_service.message.DeadlineRequestedEvent;
import com.msa.delivery_service.message.RedisStreamEventPublisher;
import com.msa.delivery_service.dto.DeliveryRequest;
import com.msa.delivery_service.dto.DeliveryResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryCreateService {
    private static final String UK_ACTIVE_ORDER_ID =
            "uk_p_deliveries_active_order_id";
    private static final String UK_ACTIVE_COMPANY_DELIVERY_MANAGER =
            "uk_p_deliveries_active_company_delivery_manager";
    private static final String UK_ACTIVE_HUB_DELIVERY_MANAGER =
            "uk_p_delivery_route_histories_active_delivery_manager";

    /*
        ?몃? ?쒕퉬???몄텧怨?DB 而ㅻ꽖?섏쓣 遺꾨━?섍린 ?꾪빐 ?몃옖??뀡???몃?濡?遺꾨━
        Self-Invocation 諛⑹?瑜??꾪빐 ?곕줈 ?대옒???묒꽦
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
        UUID departureHubId = hubRoutes.get(0).getDepartureHubId();
        UUID destinationHubId = getDestinationHubId(hubRoutes);

        try {
            Delivery delivery = Delivery.create(
                    request.getOrderId(),
                    departureHubId,
                    destinationHubId,
                    request.getReceiverCompanyId(),
                    companyDeliveryManager.getDeliveryManagerId(),
                    request.getDeliveryAddress(),
                    request.getReceiverName(),
                    hubManager.getHubManagerSlackId()
            );
            delivery.updateEstimatedArrival(calculateEstimatedArrivalAt(hubRoutes));
            Delivery savedDelivery = deliveryRepository.saveAndFlush(delivery);

            List<DeliveryRouteHistory> routeHistories = HubRouteResponse.toDeliveryRouteHistories(
                    savedDelivery,
                    companyDeliveryManager.getDeliveryManagerId(),
                    hubRoutes,
                    hubDeliveryManagerIds
            );
            deliveryRouteHistoryRepository.saveAllAndFlush(routeHistories);

            // 而ㅻ컠???꾨즺?섎㈃ 肄쒕갚?쇰줈 ?대깽??諛쒗뻾
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
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(translateIntegrityException(e));
        }
    }

    private LocalDateTime calculateEstimatedArrivalAt(List<HubRouteResponse> hubRoutes) {
        long totalEstimatedDurationMinutes = hubRoutes.stream()
                .map(HubRouteResponse::getEstimatedDurationMin)
                .filter(duration -> duration != null)
                .mapToLong(Integer::longValue)
                .sum();
        return LocalDateTime.now().plusMinutes(totalEstimatedDurationMinutes);
    }

    private UUID getDestinationHubId(List<HubRouteResponse> hubRoutes) {
        return hubRoutes.get(hubRoutes.size() - 1).getDepartureHubId();
    }

    // ?꾩옱 ?좊땲???쒖빟??珥?3怨녹뿉 ?곸슜?섏뼱 ?덉쑝誘濡?援щ텇?섍린 ?꾪븳 硫붿꽌??    // Delivery 以묐났 ?쒖빟
    // ?낆껜 諛곗넚 ?대떦 湲곗궗 ?쒖빟
    // ?덈툕 諛곗넚 ?대떦 湲곗궗 ?쒖빟
    private DeliveryErrorCode translateIntegrityException(DataIntegrityViolationException e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolationException) {
                String constraintName = constraintViolationException.getConstraintName();
                if (UK_ACTIVE_ORDER_ID.equals(constraintName)) {
                    return DeliveryErrorCode.DUPLICATE_ORDER_DELIVERY;
                }
                if (UK_ACTIVE_COMPANY_DELIVERY_MANAGER.equals(constraintName)
                        || UK_ACTIVE_HUB_DELIVERY_MANAGER.equals(constraintName)) {
                    return DeliveryErrorCode.DELIVERY_ASSIGNMENT_CONFLICT;
                }
                break;
            }
            cause = cause.getCause();
        }

        return DeliveryErrorCode.DUPLICATE_ORDER_DELIVERY;
    }
}
