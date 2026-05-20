package com.msa.hub_service.client;

import com.msa.hub_service.dto.CoordinateDto;
import com.msa.hub_service.dto.KakaoAddressResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Slf4j
@Component
public class KakaoGeocodingAdapter implements AddressGeocodingPort {

    private final RestClient restClient;
    private final String kakaoRestApiKey;

    public KakaoGeocodingAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${kakao.rest-api-key}") String kakaoRestApiKey
    ) {
        this.restClient = restClientBuilder.baseUrl("https://dapi.kakao.com").build();
        this.kakaoRestApiKey = kakaoRestApiKey;
    }

    @Override
    @RateLimiter(name = "kakaoApi")
    @Retry(name = "kakaoApi", fallbackMethod = "fallbackCoordinate")
    public CoordinateDto getCoordinate(String address) {

        // 1. RestClient를 이용한 API 호출
        KakaoAddressResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/address.json")
                        .queryParam("query", address)
                        .build())
                .header("Authorization", "KakaoAK " + kakaoRestApiKey)
                .retrieve()
                .body(KakaoAddressResponse.class); // 정의한 DTO로 자동 매핑

        // 2. 응답 검증 (결과가 없는 경우)
        if (response == null || response.documents() == null || response.documents().isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 주소이거나 좌표를 찾을 수 없습니다: " + address);
        }

        // 3. String으로 받은 위경도를 BigDecimal로 변환
        KakaoAddressResponse.Document document = response.documents().get(0);
        BigDecimal longitude = new BigDecimal(document.longitude());
        BigDecimal latitude = new BigDecimal(document.latitude());

        // 4. 공통 DTO로 반환
        return new CoordinateDto(latitude, longitude);
    }

    private CoordinateDto fallbackCoordinate(String address, Exception e) {
        log.error("[Fallback] 카카오 API 호출 최종 실패. 주소: {}, 사유: {}", address, e.getMessage());

        // 방법 1. 에러를 던져서 Service 단의 catch 블록으로 넘김 (추천)
        throw new RuntimeException("카카오 지도 API 장애로 좌표 변환에 실패했습니다.", e);

    }
}
