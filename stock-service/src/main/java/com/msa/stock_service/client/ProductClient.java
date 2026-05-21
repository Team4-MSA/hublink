package com.msa.stock_service.client;

import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service")
public interface ProductClient {
    @GetMapping("/api/v1/products/byIdList")
    List<ProductResponse>  getProductsById(@RequestBody  List<UUID> productIdList);
}
