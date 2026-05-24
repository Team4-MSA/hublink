package com.msa.order_service.dto.res;

import com.msa.order_service.entity.Orders;
import com.msa.order_service.type.Status;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOrderResDto {

    private UUID orderId;

    private UUID supplierCompanyId;
    private String supplierCompanyName;

    private UUID receiverCompanyId;
    private String receiverCompanyName;

    private UUID orderedByUserId;
    private String orderedByUserName;

    private Status status;
    private Integer totalPrice;
    private String requestMemo;
    private LocalDateTime requestedDeliveryDeadline;
    private LocalDateTime createdAt;

    public static UserOrderResDto createOrdersRes(Orders order, Map<UUID, String> companyMap, Map<UUID, String> userMap) {
        return UserOrderResDto.builder()
                .orderId(order.getId())
                .supplierCompanyId(order.getSupplierCompanyId())
                .supplierCompanyName(companyMap.getOrDefault(order.getSupplierCompanyId(), "알 수 없는 공급사"))
                .receiverCompanyId(order.getReceiverCompanyId())
                .receiverCompanyName(companyMap.getOrDefault(order.getReceiverCompanyId(), "알 수 없는 수령사"))
                .orderedByUserId(order.getOrderedByUserId())
                .orderedByUserName(userMap.getOrDefault(order.getOrderedByUserId(), "알 수 없는 주문자"))
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .requestMemo(order.getRequestMemo())
                .requestedDeliveryDeadline(order.getRequestedDeliveryDeadline())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
