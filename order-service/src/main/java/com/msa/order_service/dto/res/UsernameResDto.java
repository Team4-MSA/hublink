package com.msa.order_service.dto.res;

import java.util.UUID;

public record UsernameResDto(
        UUID id,
        String name,
        UUID companyId,
        String email
) {}
