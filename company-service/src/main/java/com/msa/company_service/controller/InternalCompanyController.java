package com.msa.company_service.controller;

import com.msa.company_service.dto.CompanyDto;
import com.msa.company_service.dto.CompanyNameResponse;
import com.msa.company_service.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/companies")
@RequiredArgsConstructor
public class InternalCompanyController {
    private final CompanyService companyService;

    @GetMapping("/{companyId}/location")
    public CompanyDto getCompanyLocation(@PathVariable UUID companyId) {

        return companyService.getCompanyLoc(companyId);
    }

    @GetMapping("/{companyId}/exists")
    public Boolean checkCompanyExists(@PathVariable UUID companyId) {
        return companyService.getCompanyExists(companyId);
    }

    @GetMapping("/names")
    public List<CompanyNameResponse> getCompanyNames(
            @RequestParam("companyIds") List<UUID> companyIds
    ) {
        return companyService.getCompanyNames(companyIds);
    }
}
