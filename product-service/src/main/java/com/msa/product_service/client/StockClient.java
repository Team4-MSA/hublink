package com.msa.product_service.client;

import com.msa.core_common.response.GlobalResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "stock-service")
public interface StockClient {
    @PostMapping("/api/v1/stocks/history")
    Void createStockHistory(StockRequestDto dto);
}
