package com.msa.order_service.dto.req;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModifyStockReqDto {

    UUID orderId;

    String ordererName;

    String ordererEmail;

    String deliveryAddress;

    String receiverCompanyName;

    private List<OrderMakeReqDto.Items> items;


}
