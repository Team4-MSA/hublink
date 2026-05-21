package com.msa.ai_service.exception;

import com.msa.core_common.error.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
@Getter
@RequiredArgsConstructor
public enum AiErrorCode implements ErrorCode {
    AI_MESSAGE_NOT_FOUND("AI_404_001", HttpStatus.NOT_FOUND, "AI 생성 이력을 찾을 수 없습니다."),
    AI_MESSAGE_ACCESS_DENIED("AI_403_001", HttpStatus.FORBIDDEN, "AI 생성 이력 접근 권한이 없습니다."),
    AI_REQUEST_FAILED("AI_500_001", HttpStatus.INTERNAL_SERVER_ERROR, "AI 요청 처리에 실패했습니다."),
    AI_EVENT_PUBLISH_FAILED("AI_500_002", HttpStatus.INTERNAL_SERVER_ERROR, "AI 생성 완료 이벤트 발행에 실패했습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}

