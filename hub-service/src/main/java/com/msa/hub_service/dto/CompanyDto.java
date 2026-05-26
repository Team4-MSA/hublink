package com.msa.hub_service.dto;

import java.math.BigDecimal;

public record CompanyDto(
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
