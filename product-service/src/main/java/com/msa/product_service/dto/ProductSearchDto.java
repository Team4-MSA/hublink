package com.msa.product_service.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchDto {
    private String productName;
    private Integer maxPrice;
    private Integer minPrice;
    private UUID hubId;
}
