package com.msa.company_service.client;

import com.msa.company_service.dto.CoordinateDto;
import com.msa.company_service.global.CompanyErrorCode;
import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.GlobalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HubClientFallbackFactory implements FallbackFactory<HubClient> {
    private static final Logger log = LoggerFactory.getLogger(HubClientFallbackFactory.class);

    @Override
    public HubClient create(Throwable cause) {
        return new HubClient() {
            @Override
            public Boolean getHubExist(UUID hubId) {
                log.error("HubService getHubExists 통신 실패 - hubId: {}, 사유: {}", hubId, cause.getMessage());

                throw new CustomException(CompanyErrorCode.HUB_SERVICE_UNAVAILABLE);
            }

            @Override
            public CoordinateDto getCoordinates(String address) {
                log.error("hub-service 통신 장애. 주소: {}, 사유: {}", address, cause.getMessage());

                CoordinateDto emptyCoordinate = new CoordinateDto(null, null);

                return emptyCoordinate;
            }
        };
    }
}
