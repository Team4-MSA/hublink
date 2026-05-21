package com.msa.hub_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.hub_service.dto.HubRouteResponse;
import com.msa.hub_service.entity.HubEntity;
import com.msa.hub_service.entity.HubRouteEntity;
import com.msa.hub_service.global.HubErrorCode;
import com.msa.hub_service.message.HubCreatedEvent;
import com.msa.hub_service.message.HubDeletedEvent;
import com.msa.hub_service.message.HubUpdatedEvent;
import com.msa.hub_service.repository.HubRepository;
import com.msa.hub_service.repository.HubRouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.AuditorAware;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class HubRouteService {

    private final HubRouteRepository hubRouteRepository;
    private final HubRepository hubRepository;
    private final AuditorAware<String> auditorAware;

    @Transactional
    public HubRouteResponse createHubRoute(UUID departureHubId, UUID arrivalHubId) {

        // 출발지 도착지 확인
        if (departureHubId.equals(arrivalHubId)) {
            throw new CustomException(HubErrorCode.SAME_HUB_NOT_ALLOWED);
        }

        //중복 경로 방지
        if (hubRouteRepository.existsByDepartureHub_HubIdAndArrivalHub_HubId(departureHubId, arrivalHubId)) {
            throw new CustomException(HubErrorCode.HUB_ROUTE_DUPLICATED);
        }

        // 허브 존재 확인
        HubEntity departureHub = hubRepository.findById(departureHubId)
                .orElseThrow(() -> new CustomException(HubErrorCode.HUB_NOT_FOUND));
        HubEntity arrivalHub = hubRepository.findById(arrivalHubId)
                .orElseThrow(() -> new CustomException(HubErrorCode.HUB_NOT_FOUND));

        // 허브 위치 확인
        if (departureHub.getLatitude() == null || departureHub.getLongitude() == null ||
                arrivalHub.getLatitude() == null || arrivalHub.getLongitude() == null) {
            throw new CustomException(HubErrorCode.NULL_COORDINATES);
        }

        HubRouteEntity hubRoute = HubRouteEntity.create(departureHub, arrivalHub);
        hubRoute = hubRouteRepository.save(hubRoute);

        return HubRouteResponse.from(hubRoute);

    }

    @Async
    @EventListener
    @Transactional
    public void creatRoutesForNewHub(HubCreatedEvent event) {
        HubEntity newHub = hubRepository.findById(event.hubId())
                .orElseThrow(() -> new CustomException(HubErrorCode.HUB_NOT_FOUND));

        List<HubEntity> existingHubs = hubRepository.findByHubIdNot(newHub.getHubId());

        List<HubEntity> validExistingHubs = existingHubs.stream()
                .filter(hub -> hub.getLatitude() != null && hub.getLongitude() != null)
                .toList();


        if (validExistingHubs.isEmpty()) {
            return;
        }

        List<HubRouteEntity> newRoutes = new ArrayList<>();

        for (HubEntity existingHub : validExistingHubs) {
            newRoutes.add(HubRouteEntity.create(newHub, existingHub));
            newRoutes.add(HubRouteEntity.create(existingHub, newHub));
        }

        hubRouteRepository.saveAll(newRoutes);
    }

    @Async
    @EventListener
    @Transactional
    public void updateRoutesForUpdatedHub(HubUpdatedEvent event) {
        List<HubRouteEntity> affectedRoutes = hubRouteRepository.findByInvolvedHubId(event.hubId());

        if (affectedRoutes.isEmpty()) {
            //기존 경로가 없지만 수정된 경우
            creatRoutesForNewHub(new HubCreatedEvent(event.hubId()));
            return;
        }

        for (HubRouteEntity route : affectedRoutes) {
            route.recalculateRouteInfo();
        }

        hubRouteRepository.saveAll(affectedRoutes);
    }

    @Async
    @EventListener
    @Transactional
    public void deleteRoutesForDeletedHub(HubDeletedEvent event){
        List<HubRouteEntity> affectedRoutes = hubRouteRepository.findByInvolvedHubId(event.hubId());

        if (affectedRoutes.isEmpty()) {return;}

        String deletedBy = auditorAware.getCurrentAuditor().orElse("SYSTEM");

        for (HubRouteEntity route : affectedRoutes) {
            route.delete(deletedBy);
        }
    }
}
