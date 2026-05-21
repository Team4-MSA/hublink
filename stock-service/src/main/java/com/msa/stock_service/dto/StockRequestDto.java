package com.msa.stock_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockRequestDto {
    @NotBlank(message = "상품을 지정하시오.")
    private UUID productId;
    @NotBlank(message = "허브를 지정하시오.")
    private UUID hubId;
    @NotBlank(message = "수량을 입력하시오.")
    @Min(value = 0,message = "최소 0개 이상 등록 해야합니다.")
    private Integer quantity;
}
