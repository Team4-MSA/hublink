package com.msa.hub_service.client;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.GlobalResponse;
import com.msa.hub_service.dto.CompanyDto;
import com.msa.hub_service.global.HubErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class CompanyClientFallbackFactory implements FallbackFactory<CompanyClient> {
    @Override
    public CompanyClient create(Throwable cause) {
        return new CompanyClient() {
            @Override
            public CompanyDto getCompanyLocation(UUID companyId) {
                throw new CustomException(HubErrorCode.COMPANY_SERVICE_UNAVAILABLE);
            }
        };
    }
}
