package com.msa.user_service.dto;

import com.msa.user_service.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class InternalHubManagerResponse {

    private UUID userId;
    private UUID hubId;
    private String hubManagerSlackId;

    public static InternalHubManagerResponse of(User user) {
        return InternalHubManagerResponse.builder()
                .userId(user.getUserId())
                .hubId(user.getHubId())
                .hubManagerSlackId(user.getSlackId())
                .build();
    }
}
