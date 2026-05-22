package com.msa.product_service.client;

import com.msa.core_common.response.GlobalResponse;
import java.util.Map;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {
    // 주소는 추후에 추가할 예정
    // 허브 담당자 본인 확인 여부
    @GetMapping("/internal/users/{userId}/hub/{hubId}/verify")
    GlobalResponse<Map<String,Boolean>> IsHubManager(@PathVariable("userId")UUID userId, @PathVariable("hubId")UUID hubId);
    // 업체 담당자 본인 확인 여부
    @GetMapping("/internal/users/{userId}/company/{companyId}/verify")
    GlobalResponse<Map<String,Boolean>> IsCompanyManager(@PathVariable("userId")UUID userId, @PathVariable("companyId")UUID companyId);
}
