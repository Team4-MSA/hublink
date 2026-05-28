package com.msa.product_service.client;

import com.msa.core_common.auth.UserRole;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResponseDto {
    private UUID userId;
    private String username;
    private String name;
    private String email;
    private UserRole role;
    private UUID hubId;
}
