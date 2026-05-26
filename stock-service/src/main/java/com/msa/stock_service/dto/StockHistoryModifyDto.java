package com.msa.stock_service.dto;

import com.msa.stock_service.entity.StockChangeReason;
import com.msa.stock_service.entity.StockHistory;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockHistoryModifyDto {

    private UUID stockId;
    private UUID productId;
    private UUID hubId;
    private Integer quantity;
    private StockChangeReason reason;
    private LocalDateTime updatedAt;

    public static StockHistoryModifyDto from(StockHistory history) {
        return StockHistoryModifyDto.builder()
            .stockId(history.getStockId())
            .productId(history.getProductId())
            .hubId(history.getHubId())
            .quantity(history.getAfterQuantity())
            .reason(history.getReason())
            .updatedAt(history.getUpdatedAt())
            .build();
    }
}
