package com.msa.user_service.dto;

import com.msa.user_service.entity.HubManager;
import com.msa.user_service.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class InternalHubManagerResponse {

    private UUID hubManagerId;
    private UUID hubId;
    private String hubManagerSlackId;

    public static InternalHubManagerResponse of(HubManager hubManager, User user) {
        return InternalHubManagerResponse.builder()
                .hubManagerId(hubManager.getHubManagerId())
                .hubId(hubManager.getHubId())
                .hubManagerSlackId(user.getSlackId())
                .build();
    }
}
