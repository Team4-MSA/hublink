package com.msa.order_service.dto.res;

import java.util.UUID;

public record CompanyNameResDto(
        UUID id,
        String name
) {
}
