package com.msa.user_service.dto;

import com.msa.user_service.entity.DeliveryManagerType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class DeliveryManagerRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID hubId;

    @NotNull
    private DeliveryManagerType type;
}
