package com.msa.user_service.dto;

import com.msa.user_service.entity.HubManager;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class HubManagerResponse {

    private UUID hubManagerId;
    private UUID userId;
    private UUID hubId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static HubManagerResponse from(HubManager hubManager) {
        return HubManagerResponse.builder()
                .hubManagerId(hubManager.getHubManagerId())
                .userId(hubManager.getUserId())
                .hubId(hubManager.getHubId())
                .createdAt(hubManager.getCreatedAt())
                .updatedAt(hubManager.getUpdatedAt())
                .build();
    }
}
