package com.msa.delivery_service.domain.enums;

import com.msa.core_common.error.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DeliveryErrorCode implements ErrorCode {

    // 400
    INVALID_DELIVERY_STATUS_TRANSITION("DELIVERY_001", "변경할 수 없는 배송 상태입니다.", HttpStatus.BAD_REQUEST),
    INVALID_ROUTE_STATUS_TRANSITION("DELIVERY_002", "변경할 수 없는 배송 경로 상태입니다.", HttpStatus.BAD_REQUEST),

    // 403
    ACCESS_DENIED("DELIVERY_003", "배송 기능 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 404
    NO_DELIVERY_MANAGER("DELIVERY_004", "배정 가능한 배송 담당자가 없습니다.", HttpStatus.NOT_FOUND),
    NO_HUB_ROUTE("DELIVERY_005", "배송에 사용할 허브 경로를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DELIVERY_NOT_FOUND("DELIVERY_006", "배송을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DELIVERY_ROUTE_HISTORY_NOT_FOUND("DELIVERY_007", "배송 경로 이력을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NO_HUB_MANAGER("DELIVERY_010", "허브 관리자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 409
    DUPLICATE_ORDER_DELIVERY("DELIVERY_008", "이미 배송이 생성된 주문입니다.", HttpStatus.CONFLICT),

    // 500
    AI_SCHEDULE_EVENT_PUBLISH_FAILED("DELIVERY_009", "AI 최종 발송 시한 요청 이벤트 발행에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 502
    USER_SERVICE_UNAVAILABLE("DELIVERY_011", "사용자 서비스와 통신할 수 없습니다.", HttpStatus.BAD_GATEWAY),
    HUB_SERVICE_UNAVAILABLE("DELIVERY_013", "허브 서비스와 통신할 수 없습니다.", HttpStatus.BAD_GATEWAY);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
