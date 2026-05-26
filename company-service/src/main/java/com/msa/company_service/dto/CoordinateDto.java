package com.msa.company_service.dto;

import java.math.BigDecimal;

public record CoordinateDto(
        BigDecimal latitude,
        BigDecimal longitude
) {
}
