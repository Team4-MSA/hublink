package com.msa.user_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class CompanyManagerRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID companyId;
}
