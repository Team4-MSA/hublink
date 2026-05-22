package com.msa.slack_service.exception;

import com.msa.core_common.error.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SlackErrorCode implements ErrorCode {
    SLACK_MESSAGE_NOT_FOUND("SLACK_404_001", HttpStatus.NOT_FOUND, "슬랙 메시지를 찾을 수 없습니다."),
    SLACK_MESSAGE_SEND_FAILED("SLACK_500_001", HttpStatus.INTERNAL_SERVER_ERROR, "슬랙 메시지 전송에 실패했습니다."),
    SLACK_MESSAGE_ALREADY_SENT("SLACK_409_001", HttpStatus.CONFLICT, "이미 발송된 슬랙 메시지입니다."),
    SLACK_MESSAGE_ACCESS_DENIED("SLACK_403_001", HttpStatus.FORBIDDEN, "슬랙 메시지 접근 권한이 없습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
