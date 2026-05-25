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

    // 404 NOT_FOUND: 리소스를 찾을 수 없음
    HUB_NOT_FOUND("COM-404-1", "해당 허브를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    COMPANY_NOT_FOUND("COM-404-2", "해당 업체를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 409 CONFLICT: 리소스 충돌 (중복)
    COMPANY_NAME_DUPLICATED("COM-409-1", "이미 존재하는 업체입니다.", HttpStatus.CONFLICT),

    // 503
    HUB_SERVICE_UNAVAILABLE("COM-503-1", "허브 서비스가 현재 일시적으로 사용 불가능합니다. 잠시 후 다시 시도해 주세요.", HttpStatus.SERVICE_UNAVAILABLE);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
