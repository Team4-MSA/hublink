package com.msa.delivery_service.presentation.dto;

import com.msa.delivery_service.domain.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryStatusUpdateRequest {

    @NotNull
    private DeliveryStatus status;
}
