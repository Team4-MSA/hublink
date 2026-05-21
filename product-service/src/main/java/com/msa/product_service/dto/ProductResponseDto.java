package com.msa.product_service.dto;

import com.msa.product_service.entity.Product;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {
    private UUID productId;
    private UUID companyId;
    private UUID hubId;
    private String name;
    private String description;
    private Integer price;

    public static ProductResponseDto from(Product product){
        return ProductResponseDto.builder()
            .productId(product.getId())
            .companyId(product.getCompanyId())
            .hubId(product.getHubId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .build();
    }
}
