package com.msa.stock_service.entity;

public enum StockChangeReason {
    ORDER_CREATED,  // 주문 생성으로 인한 차감.
    ORDER_CANCELED, // 주문 취소로 인한 복구.
    PRODUCT_CREATED, // 상품 생성으로 인한 증가
    OUT_OF_STOCK // 주문수량이  남은 수량보다 초과된 경우
}
