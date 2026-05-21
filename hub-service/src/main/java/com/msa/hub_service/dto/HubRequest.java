package com.msa.hub_service.dto;

import jakarta.validation.constraints.NotNull;

public record HubRequest (
        @NotNull(message = "허브 이름은 필수입니다.")
        String name,

        @NotNull(message = "허브 주소는 필수입니다.")
        String address
){}
