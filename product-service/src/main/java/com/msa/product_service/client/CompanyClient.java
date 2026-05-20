package com.msa.product_service.client;

import com.msa.core_common.response.GlobalResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient
public interface CompanyClient {

    @GetMapping("/api/v1/companies/{companyId}")
    CompanyResponseDto getCompany(@PathVariable UUID companyId);
}
