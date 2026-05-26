package com.msa.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyResponse {

    private boolean verified;

    public static VerifyResponse of(boolean verified) {
        return new VerifyResponse(verified);
    }
}
