package com.msa.order_service.dto.req;

import com.msa.order_service.dto.res.UsernameResDto;
import com.msa.order_service.entity.Orders;
import com.msa.order_service.type.Status;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class MakeDeliveryReqDto {

    public UUID orderId;

    public String ordererName;

    public String ordererEmail;

    public LocalDateTime orderedAt;

    public String requestMessage;

    public List<Products> products;

    public UUID supplyCompanyId;

    public UUID receiverCompanyId;

    public String deliveryAddress;

    public String receiverName;

    public LocalDateTime requestedArrivalAt;

    @Data
    public static class Products {
        public String productName;

        public Integer quantity;
    }

    public static MakeDeliveryReqDto from(Orders order, UsernameResDto orderer, String deliveryAddress, String receiverName) {
        MakeDeliveryReqDto dto = new MakeDeliveryReqDto();
        dto.setOrderId(order.getId());
        dto.setOrdererName(orderer.name());
        dto.setOrdererEmail(orderer.email());
        dto.setOrderedAt(order.getCreatedAt());
        dto.setRequestMessage(order.getRequestMemo());
        dto.setSupplyCompanyId(order.getSupplierCompanyId());
        dto.setReceiverCompanyId(order.getReceiverCompanyId());
        dto.setDeliveryAddress(deliveryAddress);
        dto.setReceiverName(receiverName);
        dto.setRequestedArrivalAt(order.getRequestedDeliveryDeadline());

        // 재고 차감에 성공한 품목들만 배송 상품 목록으로 변환
        List<Products> deliveryProducts = order.getOrderItems().stream()
                .filter(item -> item.getStatus() != Status.FAILED)
                .map(item -> {
                    Products p = new Products();
                    p.setProductName(item.getProductName());
                    p.setQuantity(item.getQuantity());
                    return p;
                }).toList();

        dto.setProducts(deliveryProducts);
        return dto;
    }
}
