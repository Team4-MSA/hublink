package com.msa.stock_service.dto;

import com.msa.stock_service.entity.Stock;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponseDto {
    private UUID stockId;
    private UUID productId;
    private UUID hubId;
    private Integer quantity;
    private Integer reservedQuantity;

    public static StockResponseDto from(Stock stock){
        return StockResponseDto.builder()
            .stockId(stock.getId())
            .productId(stock.getProductId())
            .hubId(stock.getHubId())
            .quantity(stock.getQuantity())
            .reservedQuantity(stock.getReservedQuantity())
            .build();
    }

}
