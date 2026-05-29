package com.msa.delivery_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @NotBlank
    private String requestMessage;

    @Valid
    @NotEmpty
    private List<Product> products;

    @NotNull
    private UUID supplyCompanyId;

    @NotNull
    private UUID receiverCompanyId;

    @NotBlank
    private String deliveryAddress;

    @NotBlank
    private String receiverName;

    private LocalDateTime requestedArrivalAt;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Product {

        @NotBlank
        private String productName;

        @NotNull
        @Positive
        private Integer quantity;
    }
}
