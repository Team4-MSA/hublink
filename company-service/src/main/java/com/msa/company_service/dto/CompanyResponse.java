package com.msa.company_service.dto;

import com.msa.company_service.entity.CompanyEntity;
import com.msa.company_service.entity.CompanyType;

import java.math.BigDecimal;
import java.util.UUID;

public record CompanyResponse(
        UUID companyId,
        UUID hubId,
        String name,
        CompanyType type,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {
    public static CompanyResponse from(CompanyEntity company) {
        return new CompanyResponse(
                company.getCompanyId(),
                company.getHubId(),
                company.getName(),
                company.getType(),
                company.getAddress(),
                company.getLatitude(),
                company.getLongitude()
        );
    }
}
