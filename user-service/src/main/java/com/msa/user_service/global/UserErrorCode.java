package com.msa.user_service.global;

import com.msa.core_common.error.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    // 400 BAD_REQUEST
    PASSWORD_MISMATCH("USER-400-1", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    NOT_APPROVED("USER-400-2", "승인되지 않은 계정입니다.", HttpStatus.BAD_REQUEST),
    NOT_PENDING_STATUS("USER-400-3", "PENDING 상태의 유저만 승인/거절 가능합니다.", HttpStatus.BAD_REQUEST),
    DELIVERY_TYPE_REQUIRED("USER-400-4", "배송 타입은 필수입니다.", HttpStatus.BAD_REQUEST),

    // 401 UNAUTHORIZED
    LOGIN_REQUIRED("USER-401-1", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("USER-401-2", "토큰이 유효하지 않습니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN("USER-401-3", "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),

    // 403 FORBIDDEN
    ACCESS_DENIED("USER-403-1", "권한이 없습니다.", HttpStatus.FORBIDDEN),
    HUB_ACCESS_DENIED("USER-403-2", "담당 허브에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN),
    SELF_ONLY_ACCESS("USER-403-3", "본인 정보만 조회할 수 있습니다.", HttpStatus.FORBIDDEN),
    NO_ASSIGNED_HUB("USER-403-4", "담당 허브가 없습니다.", HttpStatus.FORBIDDEN),

    // 404 NOT_FOUND
    USER_NOT_FOUND("USER-404-1", "유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    HUB_MANAGER_NOT_FOUND("USER-404-2", "허브 관리자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COMPANY_MANAGER_NOT_FOUND("USER-404-3", "업체 담당자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DELIVERY_MANAGER_NOT_FOUND("USER-404-4", "배송 담당자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 409 CONFLICT
    DUPLICATE_USERNAME("USER-409-1", "이미 사용중인 username입니다.", HttpStatus.CONFLICT),
    DUPLICATE_EMAIL("USER-409-2", "이미 사용중인 email입니다.", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
