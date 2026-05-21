package com.msa.stock_service.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockDecreaRequestDto {
    private UUID productId;
    private Integer quantity;
}
