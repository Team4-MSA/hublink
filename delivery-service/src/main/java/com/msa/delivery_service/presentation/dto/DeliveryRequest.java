package com.msa.delivery_service.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class DeliveryRequest {

    @NotNull
    private UUID orderId;

    @NotBlank
    private String ordererName;

    @Email
    @NotBlank
    private String ordererEmail;

    @NotNull
    private LocalDateTime orderedAt;

    private String requestMessage;

    @Valid
    @NotEmpty
    private List<Product> products;

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

    private LocalDateTime requestedArrivalAt;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Product {

        @NotBlank
        private String productName;

        @NotNull
        @Positive
        private Integer quantity;
    }
}
