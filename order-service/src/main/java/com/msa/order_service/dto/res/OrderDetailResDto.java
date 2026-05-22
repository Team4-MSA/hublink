package com.msa.order_service.dto.res;

import com.msa.order_service.entity.OrderItems;
import com.msa.order_service.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResDto {

    private UUID orderId;
    private UUID supplierCompanyId;
    private UUID receiverCompanyId;
    private UUID orderedByUserId;
    private String status;
    private Integer totalPrice;
    private String requestMemo;
    private LocalDateTime requestedDeliveryDeadline;
    private List<OrderItemDto> items; // 자식 리스트 매핑
    private LocalDateTime createdAt;

    public static OrderDetailResDto from(Orders order) {
        List<OrderItemDto> itemDtos = order.getOrderItems().stream()
                .map(OrderItemDto::from)
                .toList();

        return OrderDetailResDto.builder()
                .orderId(order.getId())
                .supplierCompanyId(order.getSupplierCompanyId())
                .receiverCompanyId(order.getReceiverCompanyId())
                .orderedByUserId(order.getOrderedByUserId())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .requestMemo(order.getRequestMemo())
                .requestedDeliveryDeadline(order.getRequestedDeliveryDeadline())
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private UUID orderItemId;
        private UUID productId;
        private String productName;
        private UUID supplierCompanyId;
        private String supplierCompanyName;
        private UUID hubId;
        private Integer quantity;
        private Integer unitPrice;
        private Integer totalPrice;
        private String status;

        public static OrderItemDto from(OrderItems item) {
            return OrderItemDto.builder()
                    .orderItemId(item.getId())
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .supplierCompanyId(item.getSupplierCompanyId())
                    .supplierCompanyName(item.getSupplierCompanyName())
                    .hubId(item.getHubId())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .totalPrice(item.getTotalPrice())
                    .status(item.getStatus().name())
                    .build();
        }
    }
}