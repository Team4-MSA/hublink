package com.msa.order_service.dto.res;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CompanyAddressResDto {

    public String address;

    public BigDecimal latitude;

    public BigDecimal longitude;

}
