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
public class StockHistorySearchResponseDto {
    private UUID productId;
    private UUID stockId;
    private UUID hubId;
    private Integer changeQuantity;
    private Integer beforeQuantity;
    private Integer afterQuantity;
    private StockChangeReason reason;
    private LocalDateTime createdAt;
    private String createdBy;

    public static StockHistorySearchResponseDto from(StockHistory history) {
        return StockHistorySearchResponseDto.builder()
            .productId(history.getProductId())
            .stockId(history.getStockId())
            .hubId(history.getHubId())
            .changeQuantity(history.getChangeQuantity())
            .beforeQuantity(history.getBeforeQuantity())
            .afterQuantity(history.getAfterQuantity())
            .reason(history.getReason())
            .createdAt(history.getCreatedAt())
            .createdBy(history.getCreatedBy())
            .build();
    }
}
