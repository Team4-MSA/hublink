package com.msa.user_service.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LogInResponse {

    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private String username;
    private String role;
}
