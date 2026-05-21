package com.msa.user_service.dto;

import com.msa.user_service.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class SignUpRequest {

    @NotBlank
    @Pattern(regexp = "^[a-z0-9]{4,10}$",
            message = "ID는 4~10자, 소문자와 숫자만 가능합니다")
    private String username;

    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$",
            message = "password는 8~15자, 대소문자/숫자/특수문자를 포함해야 합니다")
    private String password;

    @NotBlank
    private String name;

    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "유효한 이메일 형식이 아닙니다"
    )
    private String email;

    @NotBlank
    private String slackId;

    @NotNull
    private UserRole role;

    // role에 따라 하나만 입력
    private UUID hubId;
    private UUID companyId;
}
