package com.msa.company_service.global;

import com.msa.core_common.error.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CompanyErrorCode implements ErrorCode {

    // 400 BAD_REQUEST:
    COMPANY_NAME_REQUIRED("COM-400-1", "업체 이름은 필수입니다.", HttpStatus.BAD_REQUEST),

    // 401 UNAUTHORIZED: 인증 실패 (로그인 안 됨, 헤더 누락 등)
    UNAUTHORIZED("COM-401-1", "인증 정보가 존재하지 않거나 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),

    // 403 FORBIDDEN: 인가 실패 (권한 부족, 소유권 없음)
    FORBIDDEN("COM-403-1", "해당 기능을 실행할 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 404 NOT_FOUND: 리소스를 찾을 수 없음
    HUB_NOT_FOUND("COM-404-1", "해당 허브를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COMPANY_NOT_FOUND("COM-404-2", "해당 업체를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 409 CONFLICT: 리소스 충돌 (중복)
    COMPANY_NAME_DUPLICATED("COM-409-1", "이미 존재하는 업체입니다.", HttpStatus.CONFLICT),

    // 500 INTERNAL_SERVER_ERROR
    INTERNAL_SERVER_ERROR("COM-500-1", "서버 내부 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 503
    HUB_SERVICE_UNAVAILABLE("COM-503-1", "허브 서비스가 현재 일시적으로 사용 불가능합니다. 잠시 후 다시 시도해 주세요.", HttpStatus.SERVICE_UNAVAILABLE);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
