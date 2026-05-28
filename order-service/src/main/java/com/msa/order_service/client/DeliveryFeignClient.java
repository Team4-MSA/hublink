package com.msa.order_service.client;

import com.msa.order_service.dto.req.MakeDeliveryReqDto;
import com.msa.order_service.dto.res.MakeDeliveryResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "delivery-service", path = "/internal/deliveries")
public interface DeliveryFeignClient {

    @PostMapping("/delivery")
    public MakeDeliveryResDto makeDelivery (@RequestBody MakeDeliveryReqDto makeDeliveryReqDto);
}
