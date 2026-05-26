package com.msa.product_service.client;

import com.msa.core_common.response.GlobalResponse;
import java.util.Map;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserClient {
    // 주소는 추후에 추가할 예정
    // 허브 담당자 본인 확인 여부
    @GetMapping("/internal/users/{userId}/hub/{hubId}/verify")
    GlobalResponse<Map<String,Boolean>> isHubManager(@PathVariable("userId")UUID userId, @PathVariable("hubId")UUID hubId);
    // 업체 담당자 본인 확인 여부
    @GetMapping("/internal/users/{userId}/company/{companyId}/verify")
    GlobalResponse<Map<String,Boolean>> isCompanyManager(@PathVariable("userId")UUID userId, @PathVariable("companyId")UUID companyId);
    //UserId롤 사용자 정보 가져오기
    @GetMapping("/api/v1/users/me")
    UserResponseDto getUser(@RequestHeader("X-User-Id") UUID userId);
}
