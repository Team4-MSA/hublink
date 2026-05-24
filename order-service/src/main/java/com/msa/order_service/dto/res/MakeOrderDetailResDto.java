package com.msa.order_service.dto.res;

import com.msa.order_service.type.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MakeOrderDetailResDto(
        UUID orderId,
        UUID supplierCompanyId,
        UUID receiverCompanyId,
        UUID orderedByUserId,
        Status status,
        int totalPrice,
        String requestMemo,
        LocalDateTime requestedDeliveryDeadline,
        List<OrderItemDto> items,
        LocalDateTime createdAt
) {
    public record OrderItemDto(
            UUID orderItemId,
            UUID productId,
            String productName,
            UUID supplierCompanyId,
            String supplierCompanyName,
            UUID hubId,
            int quantity,
            int unitPrice,
            int totalPrice,
            Status status
    ) {}
}