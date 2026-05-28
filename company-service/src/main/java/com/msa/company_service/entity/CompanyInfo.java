package com.msa.company_service.entity;

import java.math.BigDecimal;
import java.util.UUID;

public record CompanyInfo(
        UUID hubId,
        String name,
        CompanyType type,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {
}