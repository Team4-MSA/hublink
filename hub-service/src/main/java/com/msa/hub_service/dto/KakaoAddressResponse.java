package com.msa.hub_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record KakaoAddressResponse(
        List<Document> documents
) {
    public record Document(
            @JsonProperty("x") String longitude,
            @JsonProperty("y") String latitude
    ) {
    }
}
