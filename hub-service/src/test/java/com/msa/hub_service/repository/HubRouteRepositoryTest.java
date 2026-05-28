package com.msa.hub_service.repository;

import com.msa.hub_service.config.QueryDslConfig;
import com.msa.hub_service.entity.HubEntity;
import com.msa.hub_service.entity.HubRouteEntity;
import com.msa.hub_service.entity.RouteType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
class HubRouteRepositoryTest {

    @Autowired
    private HubRouteRepository hubRouteRepository;

    @Autowired
    private HubRepository hubRepository;

    private HubEntity departureHub;
    private HubEntity arrivalHub;
    private HubEntity anotherHub;

    @BeforeEach
    void setUp() {
        // 기존 데이터 초기화
        hubRouteRepository.deleteAllInBatch();
        hubRepository.deleteAllInBatch();

        // Given
        departureHub = hubRepository.save(
                HubEntity.create("서울 출발 허브", "서울 강남구", new BigDecimal("37.5"), new BigDecimal("127.0"))
        );
        arrivalHub = hubRepository.save(
                HubEntity.create("부산 도착 허브", "부산 해운대구", new BigDecimal("35.1"), new BigDecimal("129.1"))
        );
        anotherHub = hubRepository.save(
                HubEntity.create("대전 허브", "대전 서구", new BigDecimal("36.3"), new BigDecimal("127.3"))
        );
    }

    @Test
    @DisplayName("성공: 출발 허브와 도착 허브 ID로 경로 존재 여부를 확인한다")
    void existsByDepartureHub_HubIdAndArrivalHub_HubId_Success() {
        // given
        HubRouteEntity route = HubRouteEntity.create(departureHub, arrivalHub);
        hubRouteRepository.save(route);

        // when
        boolean isExist = hubRouteRepository.existsByDepartureHub_HubIdAndArrivalHub_HubId(
                departureHub.getHubId(), arrivalHub.getHubId()
        );
        boolean isNotExist = hubRouteRepository.existsByDepartureHub_HubIdAndArrivalHub_HubId(
                departureHub.getHubId(), anotherHub.getHubId()
        );

        // then
        assertThat(isExist).isTrue();
        assertThat(isNotExist).isFalse();
    }

    @Test
    @DisplayName("성공: 출발 허브와 도착 허브 ID로 특정 단일 경로를 조회한다")
    void findByDepartureHub_HubIdAndArrivalHub_HubId_Success() {
        // given
        HubRouteEntity expectedRoute = HubRouteEntity.create(departureHub, arrivalHub);
        expectedRoute.update(new BigDecimal("400.0"), 300, RouteType.H2H);
        hubRouteRepository.save(expectedRoute);

        // when
        Optional<HubRouteEntity> result = hubRouteRepository.findByDepartureHub_HubIdAndArrivalHub_HubId(
                departureHub.getHubId(), arrivalHub.getHubId()
        );
        Optional<HubRouteEntity> emptyResult = hubRouteRepository.findByDepartureHub_HubIdAndArrivalHub_HubId(
                departureHub.getHubId(), anotherHub.getHubId() // 존재하지 않는 조합
        );

        // then
        assertThat(result).isPresent();

        assertThat(result.get().getEstimatedDistanceKm()).isEqualByComparingTo("400.0");
        assertThat(result.get().getEstimatedDurationMin()).isEqualTo(300);
        assertThat(result.get().getRouteType()).isEqualTo(RouteType.H2H);
        assertThat(result.get().getDepartureHub().getHubId()).isEqualTo(departureHub.getHubId());

        assertThat(emptyResult).isEmpty();
    }
}