package com.msa.product_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductRequestDto {
    @NotNull(message = "등록할 업체를 지정하십시오.")
    private UUID companyId;

    @NotNull(message = "등록할 허브를 지정하십시오.")
    private UUID hubId;

    @NotBlank(message = "상품 이름을 작성하시오.")
    @Size(max = 100, message = "상품 이름은 100자를 초과할 수 없습니다.")
    private String name;

    @NotBlank(message = "상품에 대한 설명을 입력하시오.")
    private String description;

    @NotNull(message = "생산할 상품의 가격을 입력하시오.")
    @Min(value = 0, message = "상품 가격은 0원 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "생산할 수량을 입력하시오.")
    @Min(value = 0, message = "수량은 0개 이상이어야 합니다.")
    private Integer quantity;
}
