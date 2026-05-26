package com.msa.user_service.client;

import com.msa.user_service.dto.CompanyExistsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "company-service")
public interface CompanyClient {

    @GetMapping("/internal/companies/{companyId}/exists")
    CompanyExistsResponse checkCompanyExists(@PathVariable UUID companyId);
}
