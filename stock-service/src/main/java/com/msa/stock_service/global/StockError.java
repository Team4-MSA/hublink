package com.msa.stock_service.global;

import com.msa.core_common.error.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StockError implements ErrorCode {
    STOCK_NOT_FOUND("STOCK-404-1", "재고를 찾을 수가 없습니다.", HttpStatus.NOT_FOUND),
    // 500 INTERNAL_SERVER_ERROR: 데이터 정합성 오류 (서버 내부 매핑 실패)
    ORDER_DTO_NOT_MATCHED("STOCK-500-1", "재고 이력과 매칭되는 요청 DTO가 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PRODUCT_INFO_NOT_FOUND("STOCK-500-2", "재고 이력에 해당하는 상품 정보를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;
}
