package com.msa.order_service.feign.circuit;

import com.msa.core_common.error.exception.CustomException;
import com.msa.order_service.dto.req.OrderMakeReqDto;
import com.msa.order_service.dto.res.ProductNPAResDto;
import com.msa.order_service.error.OrderErrorCode;
import com.msa.order_service.feign.ProductFeignClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCircuitService {

    private final ProductFeignClient productFeignClient;

    @CircuitBreaker(name = "decreaseProductStock", fallbackMethod = "increaseFallback")
    public List<ProductNPAResDto> decreaseProductStock(List<OrderMakeReqDto.Items> items) {
        return productFeignClient.decreaseProductStock(items);
    }

    public List<ProductNPAResDto> increaseFallback(List<OrderMakeReqDto.Items> items, Throwable t) {
        log.error("[Product Service] 장애로 인해 재고 감소 요청 불가. 원인: {}", t.getMessage());

        throw new CustomException(OrderErrorCode.PRODUCT_FEIGN_FAIL);
    }

    @CircuitBreaker(name = "increaseProductStock", fallbackMethod = "decreaseFallback")
    public Boolean increaseProductStock (List<OrderMakeReqDto.Items> items) {
        return productFeignClient.increaseProductStock(items).getIsSuccess();
    }

    public Boolean decreaseFallback(List<OrderMakeReqDto.Items> items, Throwable t) {
        log.error("[Product Service] 장애로 인해 재고 복구 요청 불가. 원인: {}", t.getMessage());

        throw new CustomException(OrderErrorCode.PRODUCT_FEIGN_FAIL);
    }
}
