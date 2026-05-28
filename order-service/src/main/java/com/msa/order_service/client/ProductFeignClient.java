package com.msa.order_service.client;

import com.msa.order_service.dto.req.OrderMakeReqDto;
import com.msa.order_service.dto.res.IncreaseProductResDto;
import com.msa.order_service.dto.res.ProductNPAResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product-service", path = "/internal/products")
public interface ProductFeignClient {

    @PostMapping("/decrease-stock")
    List<ProductNPAResDto> decreaseProductStock(@RequestBody List<OrderMakeReqDto.Items> items);

    @PostMapping("/increase-stock")
    IncreaseProductResDto increaseProductStock(@RequestBody List<OrderMakeReqDto.Items> items);
}
