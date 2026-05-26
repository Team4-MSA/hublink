package com.msa.delivery_service.presentation.dto;

import com.msa.delivery_service.domain.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeliveryStatusUpdateRequest {

    @NotNull
    private DeliveryStatus status;
}
