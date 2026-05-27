package com.msa.user_service.dto;

import com.msa.user_service.entity.DeliveryManager;
import com.msa.user_service.entity.DeliveryManagerType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class DeliveryManagerResponse {

    private UUID userId;
    private UUID hubId;
    private DeliveryManagerType type;
    private Integer deliverySequence;
    private String slackId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DeliveryManagerResponse from(DeliveryManager deliveryManager) {
        return DeliveryManagerResponse.builder()
                .userId(deliveryManager.getUserId())
                .hubId(deliveryManager.getHubId())
                .type(deliveryManager.getType())
                .deliverySequence(deliveryManager.getDeliverySequence())
                .slackId(deliveryManager.getSlackId())
                .createdAt(deliveryManager.getCreatedAt())
                .updatedAt(deliveryManager.getUpdatedAt())
                .build();
    }
}
