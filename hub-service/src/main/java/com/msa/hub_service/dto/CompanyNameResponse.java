package com.msa.hub_service.dto;

import java.util.UUID;

public record CompanyNameResponse(
        UUID id,
        String name
) {
}
