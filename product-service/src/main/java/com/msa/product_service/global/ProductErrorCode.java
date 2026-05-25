package com.msa.product_service.global;

import com.msa.core_common.error.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    // 400 BAD_REQUEST: 잘못된 요청 데이터
    VALIDATION_ERROR("PRODUCT-400-1", "요청값 검증에 실패했습니다.", HttpStatus.BAD_REQUEST),
    COMPANY_NOT_IN_HUB("PRODUCT-400-2", "해당 업체는 지정된 허브에 속해있지 않습니다.", HttpStatus.BAD_REQUEST),

    // 403 FORBIDDEN: 접근 권한 없음
    ACCESS_DENIED("PRODUCT-403-1", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    HUB_ACCESS_DENIED("PRODUCT-403-2", "해당 허브에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    COMPANY_ACCESS_DENIED("PRODUCT-403-3", "해당 업체에 대한 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 404 NOT_FOUND: 리소스를 찾을 수 없음
    PRODUCT_NOT_FOUND("PRODUCT-404-1", "존재하지 않는 상품입니다.", HttpStatus.NOT_FOUND),

    // 500 INTERNAL_SERVER_ERROR: 서버 내부 오류
    INTERNAL_SERVER_ERROR("PRODUCT-500-1", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    STOCK_SERVICE_FAILED("PRODUCT-500-2", "재고 시스템 연동에 실패하여 상품 등록이 취소되었습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}