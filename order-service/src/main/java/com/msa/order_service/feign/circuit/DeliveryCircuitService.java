package com.msa.order_service.feign.circuit;

import com.msa.core_common.error.exception.CustomException;
import com.msa.order_service.dto.req.MakeDeliveryReqDto;
import com.msa.order_service.dto.res.MakeDeliveryResDto;
import com.msa.order_service.error.OrderErrorCode;
import com.msa.order_service.feign.DeliveryFeignClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryCircuitService {

    private final DeliveryFeignClient deliveryFeignClient;

    @CircuitBreaker(name = "makeDelivery", fallbackMethod = "makeDeliveryFallback")
    public MakeDeliveryResDto makeDelivery (@RequestBody MakeDeliveryReqDto makeDeliveryReqDto) {
        return deliveryFeignClient.makeDelivery(makeDeliveryReqDto);
    }

    public MakeDeliveryResDto makeDeliveryFallback (MakeDeliveryReqDto makeDeliveryReqDto, Throwable t) {
        log.error("[Delivery Service] 가 응답하지 않아 Fallback 로직이 실행됩니다. 원인: {}", t.getMessage());

        throw new CustomException(OrderErrorCode.FAIL_DELIVERY);
    }

}
