package com.msa.product_service.client;

import com.msa.core_common.response.GlobalResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
@FeignClient(name = "user-service")
public interface UserClient {
    // 허브 담당자 본인 확인 여부
    @GetMapping("~~")
    GlobalResponse<Boolean> IsHubManager(UUID userId, UUID hubId);
    // 업체 담당자 본인 확인 여부
    @GetMapping("~~")
    GlobalResponse<Boolean> IsCompanyManager(UUID userId, UUID companyId);
}
