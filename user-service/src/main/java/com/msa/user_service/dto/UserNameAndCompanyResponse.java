package com.msa.user_service.dto;

import com.msa.user_service.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserNameAndCompanyResponse {

    private UUID id;
    private String name;
    private String email;
    private UUID companyId;

    public static UserNameAndCompanyResponse from(User user) {
        return UserNameAndCompanyResponse.builder()
                .id(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .companyId(user.getCompanyId())
                .build();
    }
}
