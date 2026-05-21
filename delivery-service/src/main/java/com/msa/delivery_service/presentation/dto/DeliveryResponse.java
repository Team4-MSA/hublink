package com.msa.delivery_service.presentation.dto;

import com.msa.delivery_service.domain.entity.Delivery;
import com.msa.delivery_service.domain.enums.DeliveryStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class DeliveryResponse {

    private UUID deliveryId;
    private UUID orderId;
    private UUID departureHubId;
    private UUID destinationHubId;
    private UUID receiverCompanyId;
    private UUID companyDeliveryManagerId;
    private DeliveryStatus status;
    private String deliveryAddress;
    private String receiverName;
    private String deliveryManagerSlackId;

    public static DeliveryResponse from(Delivery delivery) {
        return DeliveryResponse.builder()
                .deliveryId(delivery.getDeliveryId())
                .orderId(delivery.getOrderId())
                .departureHubId(delivery.getDepartureHubId())
                .destinationHubId(delivery.getDestinationHubId())
                .receiverCompanyId(delivery.getReceiverCompanyId())
                .companyDeliveryManagerId(delivery.getCompanyDeliveryManagerId())
                .status(delivery.getStatus())
                .deliveryAddress(delivery.getDeliveryAddress())
                .receiverName(delivery.getReceiverName())
                .deliveryManagerSlackId(delivery.getHubManagerSlackId())
                .build();
    }
}
