package com.msa.product_service.client;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponseDto {
    private UUID id;
    private UUID hubId;
    private String name;
    private String type;
    private String address;
}
