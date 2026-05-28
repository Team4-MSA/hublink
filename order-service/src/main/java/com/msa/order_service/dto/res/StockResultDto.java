package com.msa.order_service.dto.res;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class StockResultDto {

    UUID orderId;

    List<ProductNPAResDto> products;

    String ordererName;

    String ordererEmail;

    String deliveryAddress;

    String receiverCompanyName;
}
