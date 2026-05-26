package com.msa.company_service.dto;

import com.msa.company_service.entity.CompanyType;

import java.math.BigDecimal;
import java.util.UUID;

public record CompanyUpdateRequest(
        UUID hubId,
        String name,
        CompanyType type,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
