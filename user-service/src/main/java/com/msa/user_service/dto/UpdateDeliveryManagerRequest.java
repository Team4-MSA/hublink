package com.msa.user_service.dto;

import com.msa.user_service.entity.DeliveryManagerType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class UpdateDeliveryManagerRequest {

    private UUID hubId;
    private DeliveryManagerType type;
    private String slackId;
}
