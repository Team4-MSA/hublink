package com.msa.order_service.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderMakeReqDto {

    @Schema(description = "공급업체 ID (UUID)", example = "f71f79d1-f367-48f7-b5ab-7a16d8317cf6")
    @NotNull(message = "공급업체 ID는 필수입니다.")
    private UUID supplierCompanyId;

    @Schema(description = "수령업체 ID (UUID)", example = "f71f79d1-f367-48f7-b5ab-7a16d8317cf9")
    @NotNull(message = "수령업체 ID는 필수입니다.")
    private UUID receiverCompanyId;

    @Schema(description = "주문요청 메모", example = "빠른 배송부탁드립니다.")
    private String requestMemo;

    @Schema(description = "최종 도착 시한", example = "2026-05-28T18:00:00")
    @NotNull(message = "배송 희망 기한은 필수입니다.")
    @FutureOrPresent(message = "배송 기한은 과거일 수 없습니다.")
    private LocalDateTime requestedDeliveryDeadline;

    @Schema(description = "주문할 상품 목록 (최소 1개 이상)")
    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
    @Valid
    private List<Items> items;

    @Getter
    @AllArgsConstructor
    public static class Items {
        @Schema(description = "상품 ID (UUID)", example = "3a9b8c11-7d2f-4c9e-8e41-123456789aaa")
        @NotNull(message = "상품 ID는 필수입니다.")
        UUID productId;
        @Schema(description = "주문 수량", example = "10")
        @Positive(message = "주문 수량은 1개 이상이어야 합니다.")
        int quantity;
    }

}
