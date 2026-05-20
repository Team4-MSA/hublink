package com.msa.product_service.client;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockRequestDto {
    private UUID productId;
    private UUID hubId;
    private Integer quantity;
}
