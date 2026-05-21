package com.msa.delivery_service.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class DeliveryRequest {

    @NotNull
    private UUID orderId;

    @NotNull
    private UUID departureHubId;

    @NotNull
    private UUID destinationHubId;

    @NotNull
    private UUID receiverCompanyId;

    @NotBlank
    private String deliveryAddress;

    @NotBlank
    private String receiverName;
}
