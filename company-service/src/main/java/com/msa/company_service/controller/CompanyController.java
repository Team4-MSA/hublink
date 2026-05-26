package com.msa.company_service.controller;

import com.msa.company_service.dto.CompanyRequest;
import com.msa.company_service.dto.CompanyResponse;
import com.msa.company_service.dto.CompanyUpdateRequest;
import com.msa.company_service.entity.CompanyType;
import com.msa.company_service.global.RequireRole;
import com.msa.company_service.service.CompanyService;
import com.msa.core_common.auth.UserRole;
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
    @RequireRole({UserRole.MASTER, UserRole.HUB_MANAGER})
    @PostMapping
    public CompanyResponse createCompany(
            @Valid @RequestBody CompanyRequest request,
            @RequestHeader("X-User-Role") UserRole role,
            @RequestHeader(value = "X-Hub-Id", required = false) UUID userHubId
    ) {
        return companyService.createCompany(request, role, userHubId);
    }

    // 단건 조회
    @GetMapping("/{companyId}")
    public CompanyResponse getCompany(@PathVariable UUID companyId) {
        return companyService.getCompany(companyId);
    }

    // 삭제
    @RequireRole({UserRole.MASTER, UserRole.HUB_MANAGER})
    @DeleteMapping("/{companyId}")
    public CompanyResponse deleteCompany(
            @PathVariable UUID companyId,
            @RequestHeader("X-User-Role") UserRole role,
            @RequestHeader(value = "X-Hub-Id", required = false) UUID userHubId
    ) {
        return companyService.deleteCompany(companyId, role, userHubId);
    }

    // 수정
    @RequireRole({UserRole.MASTER, UserRole.HUB_MANAGER, UserRole.COMPANY_MANAGER})
    @PatchMapping("/{companyId}")
    public CompanyResponse updateCompany(
            @PathVariable UUID companyId,
            @Valid @RequestBody CompanyUpdateRequest request,
            @RequestHeader(value = "X-Company-Id", required = false) UUID userCompanyId,
            @RequestHeader("X-User-Role") UserRole role,
            @RequestHeader(value = "X-Hub-Id", required = false) UUID userHubId
    ) {
        return companyService.updateCompany(companyId, request, userCompanyId, role, userHubId);
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
