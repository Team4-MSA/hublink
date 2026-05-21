package com.msa.product_service.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductRequestDto {
    private UUID companyId;
    private UUID hubId;
    private String name;
    private String description;
    private Integer price;
    private Integer quantity;
}
