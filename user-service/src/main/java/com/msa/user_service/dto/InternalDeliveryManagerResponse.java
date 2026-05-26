package com.msa.user_service.dto;

import com.msa.user_service.entity.DeliveryManager;
import com.msa.user_service.entity.DeliveryManagerType;
import com.msa.user_service.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class InternalDeliveryManagerResponse {

    private UUID deliveryManagerId;
    private UUID hubId;
    private String deliveryManagerName;
    private String deliveryManagerEmail;
    private DeliveryManagerType type;
    private Integer deliverySequence;

    public static InternalDeliveryManagerResponse of(DeliveryManager deliveryManager, User user) {
        return InternalDeliveryManagerResponse.builder()
                .deliveryManagerId(deliveryManager.getUserId())  // userId == deliveryManagerId (1:1)
                .hubId(deliveryManager.getHubId())
                .deliveryManagerName(user.getName())
                .deliveryManagerEmail(user.getEmail())
                .type(deliveryManager.getType())
                .deliverySequence(deliveryManager.getDeliverySequence())
                .build();
    }
}
