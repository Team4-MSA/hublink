package com.msa.hub_service.service;

import com.msa.hub_service.client.AddressGeocodingPort;
import com.msa.hub_service.dto.CoordinateDto;
import com.msa.hub_service.dto.HubResponse;
import com.msa.hub_service.entity.HubEntity;
import com.msa.hub_service.repository.HubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class HubService {
    private final HubRepository hubRepository;
    private final AddressGeocodingPort geocodingPort;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public HubResponse createHub(String name, String address) {
        HubEntity hub;

        try {
            // 1. API 호출 시도
            CoordinateDto coordinate = geocodingPort.getCoordinate(address);
            hub = HubEntity.create(name, address, coordinate.latitude(), coordinate.longitude());

        } catch (Exception e) {
            // 2. API 장애 발생 시 에러를 던지지 않고 '미완성 상태'로 저장
            log.warn("외부 API 장애로 인해 좌표 없이 허브를 저장합니다. address: {}, exception: {}", address, e.toString());
            hub = HubEntity.createPendingCoordinates(name, address);
        }

        return HubResponse.from(hubRepository.save(hub));
    }

}
