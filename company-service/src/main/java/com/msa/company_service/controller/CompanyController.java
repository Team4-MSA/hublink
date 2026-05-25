package com.msa.company_service.controller;

import com.msa.company_service.dto.CompanyRequest;
import com.msa.company_service.dto.CompanyResponse;
import com.msa.company_service.dto.CompanyUpdateRequest;
import com.msa.company_service.entity.CompanyType;
import com.msa.company_service.service.CompanyService;
import com.msa.core_common.response.paging.PageRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    // 업체 생성
    @PostMapping
    public CompanyResponse createCompany(@Valid @RequestBody CompanyRequest request) {
        return companyService.createCompany(request);
    }

    // 단건 조회
    @GetMapping("/{companyId}")
    public CompanyResponse getCompany(@PathVariable UUID companyId) {
        return companyService.getCompany(companyId);
    }

    // 삭제
    @DeleteMapping("/{companyId}")
    public CompanyResponse deleteCompany(@PathVariable UUID companyId) {
        return companyService.deleteCompany(companyId);
    }

    // 수정
    @PatchMapping("/{companyId}")
    public CompanyResponse updateCompany(@PathVariable UUID companyId, @Valid @RequestBody CompanyUpdateRequest request) {
        return companyService.updateCompany(companyId, request);
    }

    // 검색
    @GetMapping
    public PageRes<CompanyResponse> getCompanies(
            @RequestParam(required = false) UUID hubId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) CompanyType type,
            @RequestParam(required = false) String address,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return companyService.getCompanies(hubId, name, type, address, pageable);
    }
}
