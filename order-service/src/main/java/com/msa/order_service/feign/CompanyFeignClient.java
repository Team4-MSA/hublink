package com.msa.order_service.feign;

import com.msa.order_service.dto.res.CompanyNameResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "company-service", path = "/internal/companies")
public interface CompanyFeignClient {

    @GetMapping("/names")
    public List<CompanyNameResDto> getCompanyNames(@RequestParam List<UUID> companyIds);

}
