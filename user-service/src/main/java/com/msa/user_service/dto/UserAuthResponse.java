package com.msa.user_service.dto;

import com.msa.user_service.entity.User;
import com.msa.user_service.entity.UserRole;
import com.msa.user_service.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserAuthResponse {
    private UUID userId;
    private String username;
    private String name;
    private String email;
    private String slackId;
    private UserRole role;
    private UserStatus status;
    private UUID hubId;
    private UUID companyId;
    private LocalDateTime createdAt;

    public static UserAuthResponse from(User user) {
        return UserAuthResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .slackId(user.getSlackId())
                .role(user.getRole())
                .status(user.getStatus())
                .hubId(user.getHubId())
                .companyId(user.getCompanyId())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
