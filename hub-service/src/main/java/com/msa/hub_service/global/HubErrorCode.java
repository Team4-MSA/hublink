package com.msa.hub_service.global;

import com.msa.core_common.error.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HubErrorCode implements ErrorCode {

    // 400 BAD_REQUEST: 잘못된 요청 데이터
    INVALID_COORDINATES("HUB-400-1", "위도/경도 값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    HUB_NAME_REQUIRED("HUB-400-2", "허브 이름은 필수입니다.", HttpStatus.BAD_REQUEST),
    HUB_ADDRESS_REQUIRED("HUB-400-3", "허브 주소는 필수입니다.", HttpStatus.BAD_REQUEST),
    NULL_COORDINATES("HUB-400-4", "위도와 경도 값은 null일 수 없습니다.", HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_FOUND("HUB-400-5", "유효하지 않은 주소이거나 좌표를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    DEPARTURE_HUB_REQUIRED("HUB-400-6", "출발 허브 ID는 필수입니다.", HttpStatus.BAD_REQUEST),
    ARRIVAL_HUB_REQUIRED("HUB-400-7", "도착 허브 ID는 필수입니다.", HttpStatus.BAD_REQUEST),
    SAME_HUB_NOT_ALLOWED("HUB-400-8", "출발 허브와 도착 허브는 같을 수 없습니다.", HttpStatus.BAD_REQUEST),
    GEOCODING_FAILED("HUB-400-9", "좌표 변환 외부 API 통신에 실패했거나 잘못된 주소입니다.", HttpStatus.BAD_REQUEST),

    // 404 NOT_FOUND: 리소스를 찾을 수 없음
    HUB_NOT_FOUND("HUB-404-1", "존재하지 않는 허브입니다.", HttpStatus.NOT_FOUND),

    // 409 CONFLICT: 리소스 충돌 (중복)
    HUB_NAME_DUPLICATED("HUB-409-1", "이미 존재하는 허브 이름입니다.", HttpStatus.CONFLICT),
    HUB_ADDRESS_DUPLICATED("HUB-409-2", "이미 존재하는 허브 주소입니다.", HttpStatus.CONFLICT),
    HUB_ROUTE_DUPLICATED("HUB-409-3", "이미 존재하는 허브 간 경로입니다.", HttpStatus.CONFLICT),

    // 500 INTERNAL_SERVER_ERROR: 서버/외부 연동 에러
    COORDINATE_API_ERROR("HUB-500-1", "지도 API 호출 중 서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
