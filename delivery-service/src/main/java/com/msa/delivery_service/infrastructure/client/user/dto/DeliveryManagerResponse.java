package com.msa.delivery_service.infrastructure.client.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class DeliveryManagerResponse {

    private UUID deliveryManagerId;
    private UUID hubId;
    private String type;
    private Integer deliverySequence;
}
