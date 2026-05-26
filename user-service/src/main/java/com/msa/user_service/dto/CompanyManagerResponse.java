package com.msa.user_service.dto;

import com.msa.user_service.entity.CompanyManager;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CompanyManagerResponse {

    private UUID companyManagerId;
    private UUID userId;
    private UUID companyId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CompanyManagerResponse from(CompanyManager companyManager) {
        return CompanyManagerResponse.builder()
                .companyManagerId(companyManager.getCompanyManagerId())
                .userId(companyManager.getUserId())
                .companyId(companyManager.getCompanyId())
                .createdAt(companyManager.getCreatedAt())
                .updatedAt(companyManager.getUpdatedAt())
                .build();
    }
}
