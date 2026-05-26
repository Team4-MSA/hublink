package com.msa.order_service.feign.circuit;
import com.msa.order_service.dto.res.CompanyAddressResDto;
import com.msa.order_service.dto.res.CompanyNameResDto;
import com.msa.order_service.feign.CompanyFeignClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CompanyCircuitService {

    private final CompanyFeignClient companyFeignClient;

    @CircuitBreaker(name = "companyRead", fallbackMethod = "getCompanyNamesFallback")
    public List<CompanyNameResDto> getCompanyNames(List<UUID> companyIds) {
        return companyFeignClient.getCompanyNames(companyIds);
    }

    public List<CompanyNameResDto> getCompanyNamesFallback(List<UUID> companyIds, Throwable t) {
        log.error("[Company Service] 가 응답하지 않아 Fallback 로직이 실행됩니다. 원인: {}", t.getMessage());

        // 업체 서버가 죽어도 주문 목록은 나오게 "임시 업체"로 채워서 리턴
        return companyIds.stream()
                .map(id -> new CompanyNameResDto(id, "알 수 없는 업체(서버 점검 중)"))
                .toList();
    }

    @CircuitBreaker(name = "companyAddress",fallbackMethod = "companyAddressFallback")
    public CompanyAddressResDto companyAddress (UUID companyId) {
        return companyFeignClient.getCompanyAddress(companyId);
    }

    public CompanyAddressResDto companyAddressFallback (UUID companyId, Throwable t) {
        log.error("[Company Service] 가 응답하지 않아 Fallback 로직이 실행됩니다. 원인: {}", t.getMessage());

        return (CompanyAddressResDto) Map.of("address", "조회실패");
    }

}
