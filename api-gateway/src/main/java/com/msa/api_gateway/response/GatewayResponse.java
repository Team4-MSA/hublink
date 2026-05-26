package com.msa.api_gateway.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * core-common GlobalResponse와 동일한 포맷을 유지하는 Gateway 전용 응답 클래스.
 * api-gateway는 WebFlux(Netty) 기반이라 MVC 기반의 core-common을 직접 의존할 수 없음.
 * 에러 응답 JSON 구조를 통일하기 위해 동일한 필드 구조로 별도 정의.
 * 응답 포맷:
 * {
 *   "status": 401,
 *   "message": "UNAUTHORIZED",
 *   "errors": { "errorClassName": "UNAUTHORIZED", "message": "상세 메시지" }
 * }
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GatewayResponse {
    private final int status;
    private final String message;
    private final ErrorResponse errors;

    public static GatewayResponse failure(HttpStatus httpStatus, String detail) {
        String code = httpStatus.name();
        return GatewayResponse.builder()
                .status(httpStatus.value())
                .message(code)
                .errors(ErrorResponse.of(code, detail))
                .build();
    }
}
