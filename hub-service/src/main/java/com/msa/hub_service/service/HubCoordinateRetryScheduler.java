package com.msa.hub_service.service;

import com.msa.hub_service.client.AddressGeocodingPort;
import com.msa.hub_service.dto.CoordinateDto;
import com.msa.hub_service.entity.HubEntity;
import com.msa.hub_service.message.HubCreatedEvent;
import com.msa.hub_service.repository.HubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubCoordinateRetryScheduler {

    private final HubRepository hubRepository;
    private final AddressGeocodingPort geocodingPort;
    private final RedisCacheManager cacheManager;
    private final ApplicationEventPublisher eventPublisher;


    @Scheduled(cron = "0 0 * * * *")
    public void retryMissingCoordinates() {
        // 1. 위도가 없는 허브들을 모두 조회
        List<HubEntity> pendingHubs = hubRepository.findByLatitudeIsNull();

        if (pendingHubs.isEmpty()) {
            return;
        }

        log.info("좌표 누락 허브 {}건에 대해 API 재시도를 시작합니다.", pendingHubs.size());

        for (HubEntity hub : pendingHubs) {
            try {
                // 2. 외부 API 호출
                CoordinateDto coordinate = geocodingPort.getCoordinate(hub.getAddress());

                // 3. 엔티티 정보 업데이트
                hub.updateCoordinates(coordinate.latitude(), coordinate.longitude());

                // 4. DB에 저장
                hubRepository.save(hub);

                log.info("허브 [{}] 좌표 업데이트 성공", hub.getName());

                // 캐시 관리(갱신되었으로 삭제)
                Cache cache = cacheManager.getCache("hub");
                if (cache!=null){
                    cache.evict(hub.getHubId());
                }

                // 갱신된 주소로 루트 생성
                if (hub.getLatitude() != null && hub.getLongitude() != null) {
                    eventPublisher.publishEvent(new HubCreatedEvent(hub.getHubId()));
                }

            } catch (Exception e) {
                // 실패 시 건너뛰기
                log.error("허브 [{}] 좌표 업데이트 실패: {}", hub.getName(), e.getMessage());
            }
        }

    }
}