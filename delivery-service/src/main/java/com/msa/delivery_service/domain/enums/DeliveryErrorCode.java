package com.msa.delivery_service.domain.enums;

import com.msa.core_common.error.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DeliveryErrorCode implements ErrorCode {

    NO_DELIVERY_MANAGER("DELIVERY_001", "출발 허브에 배정 가능한 배송 담당자가 없습니다.", HttpStatus.NOT_FOUND),
    NO_HUB_ROUTE("DELIVERY_002", "배송에 사용할 허브 경로를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_DELIVERY_STATUS_TRANSITION("DELIVERY_003", "변경할 수 없는 배송 상태입니다.", HttpStatus.BAD_REQUEST),
    INVALID_ROUTE_STATUS_TRANSITION("DELIVERY_004", "변경할 수 없는 배송 경로 상태입니다.", HttpStatus.BAD_REQUEST),
    AI_SCHEDULE_EVENT_PUBLISH_FAILED("DELIVERY_005", "AI 배송 일정 요청 이벤트 발행에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DELIVERY_FEATURE_NOT_IMPLEMENTED("DELIVERY_006", "아직 구현되지 않은 배송 기능입니다.", HttpStatus.NOT_IMPLEMENTED);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
