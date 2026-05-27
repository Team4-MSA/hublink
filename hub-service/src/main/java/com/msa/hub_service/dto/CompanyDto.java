package com.msa.hub_service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CompanyDto(
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        UUID hubId
) {
}
