package com.msa.company_service.dto;

import com.msa.company_service.entity.CompanyType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CompanyRequest(
        @NotNull(message = "업체 소속 허브는 필수입니다.")
        UUID hubId,

        @NotNull(message = "업체 이름은 필수입니다.")
        String name,

        @NotNull(message = "업체 유형은 필수입니다.")
        CompanyType type,

        @NotNull(message = "업체 주소는 필수입니다.")
        String address,

        BigDecimal latitude,

        BigDecimal longitude
) {
}
