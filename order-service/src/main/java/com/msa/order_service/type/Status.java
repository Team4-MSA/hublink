package com.msa.order_service.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Status {

    PENDING("생성 대기"),
    CREATED("생성"),
    CANCELED("취소"),
    COMPLETED("완료"),
    FAILED("주문아이템 생성실패");

    private final String description;
}
