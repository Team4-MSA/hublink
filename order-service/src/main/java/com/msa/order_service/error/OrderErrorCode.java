package com.msa.order_service.error;

import com.msa.core_common.error.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    INVALID_INPUT_VALUE("COMMON_400", HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    PRODUCT_FEIGN_FAIL("ORDER_001", HttpStatus.INTERNAL_SERVER_ERROR, "상품 조회에 실패하였습니다."),
    NOT_EXIST_ORDER("ORDER_002", HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다."),
    ALREADY_CANCELED("ORDER_003", HttpStatus.BAD_REQUEST, "이미 취소된 주문입니다"),
    FAIL_INCREASE_STOCK("ORDER_004", HttpStatus.BAD_REQUEST, "재고 복구에 실패하여 주문 취소가 실패하였습니다");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
