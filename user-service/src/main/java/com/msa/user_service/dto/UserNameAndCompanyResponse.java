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
    private UUID companyId;
    private String email;


    public static UserNameAndCompanyResponse from(User user) {
        return UserNameAndCompanyResponse.builder()
                .id(user.getUserId())
                .name(user.getName())
                .companyId(user.getCompanyId())
                .email(user.getEmail())
                .build();
    }
}
