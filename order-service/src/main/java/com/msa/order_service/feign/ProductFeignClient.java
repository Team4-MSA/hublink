package com.msa.order_service.feign;

import com.msa.order_service.dto.req.OrderMakeReqDto;
import com.msa.order_service.dto.res.IncreaseProductResDto;
import com.msa.order_service.dto.res.ProductNPAResDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "product-service", path = "/internal/products")
public interface ProductFeignClient {

    @PostMapping("/decrease-stock")
    List<ProductNPAResDto> decreaseProductStock(@RequestBody List<OrderMakeReqDto.Items> items);

    @PostMapping("/increase-stock")
    IncreaseProductResDto increaseProductStock(@RequestBody List<OrderMakeReqDto.Items> items);
}
