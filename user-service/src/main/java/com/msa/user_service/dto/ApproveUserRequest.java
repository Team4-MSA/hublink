package com.msa.user_service.dto;

import com.msa.user_service.entity.DeliveryManagerType;
import com.msa.user_service.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApproveUserRequest {
    @NotNull
    private UserStatus status;

    private String reason;

    // DELIVERY_MANAGER 승인 시만 필수
    private DeliveryManagerType deliveryManagerType;
}
