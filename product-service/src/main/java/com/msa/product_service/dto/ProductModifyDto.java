package com.msa.product_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductModifyDto {
    @NotNull(message = "업체를 지정하시오.")
    private UUID companyId;
    @NotNull(message = "허브를 지정하시오.")
    private UUID hubId;
    @NotBlank(message = "상품 이름을 작성하시오.")
    private String name;
    @NotBlank(message = "상품 설명을 작성하시오.")
    private String description;
    @NotNull(message = "가격을 작성하시오.")
    @Min(value = 0, message = "가격은 최소 0원 이상입니다.")
    private Integer price;
}
