package com.msa.order_service.dto.res;


import java.util.UUID;

public record ProductNPAResDto(
        UUID productId,
        boolean isSuccess,        // 재고 차감 성공 여부
        String failureReason,     // 실패 (예: "OUT_OF_STOCK", "NOT_FOUND")
        String name,              // 상품명
        Integer price,            // 개당 가격
        UUID hubId
){
}
