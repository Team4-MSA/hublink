package com.msa.hub_service.client;

import com.msa.core_common.response.GlobalResponse;
import com.msa.hub_service.dto.CompanyDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "company-service", fallbackFactory = CompanyClientFallbackFactory.class)
public interface CompanyClient {
    @GetMapping("/internal/companies/{companyId}/location")
    GlobalResponse<CompanyDto> getCompanyLocation(@PathVariable UUID companyId);
}
