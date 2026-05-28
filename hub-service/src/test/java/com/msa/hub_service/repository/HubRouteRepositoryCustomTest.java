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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@DataJpaTest
@Import(QueryDslConfig.class)
class HubRouteRepositoryCustomTest {

    @Autowired
    private HubRouteRepository hubRouteRepository;

    @Autowired
    private HubRepository hubRepository;

    private HubEntity hubA;
    private HubEntity hubB;
    private HubEntity hubC;
    private HubEntity hubD;
    private HubEntity hubE;

    @BeforeEach
    void setUp() {
        hubRouteRepository.deleteAllInBatch();
        hubRepository.deleteAllInBatch();

        // Given
        hubA = hubRepository.save(HubEntity.create("허브A", "주소A", new BigDecimal("37.1"), new BigDecimal("127.1")));
        hubB = hubRepository.save(HubEntity.create("허브B", "주소B", new BigDecimal("36.1"), new BigDecimal("127.2")));
        hubC = hubRepository.save(HubEntity.create("허브C", "주소C", new BigDecimal("35.1"), new BigDecimal("127.3")));
        hubD = hubRepository.save(HubEntity.create("허브D", "주소D", new BigDecimal("34.1"), new BigDecimal("127.4")));
        hubE = hubRepository.save(HubEntity.create("허브E", "주소E", new BigDecimal("33.1"), new BigDecimal("127.5")));
    }

    private HubRouteEntity createTestRoute(HubEntity dep, HubEntity arr, double km, int min, RouteType type) {
        HubRouteEntity route = HubRouteEntity.create(dep, arr);
        route.update(BigDecimal.valueOf(km), min, type);
        return route;
    }

    @Test
    @DisplayName("성공: 검색 조건(타입 필터링) 및 페이징/내림차순 정렬에 따라 경로를 정확히 조회한다")
    void searchHubRoutes_Success() {
        // given
        HubRouteEntity route1 = createTestRoute(hubA, hubB, 100.0, 60, RouteType.H2H);
        HubRouteEntity route2 = createTestRoute(hubA, hubC, 150.0, 90, RouteType.P2P);
        HubRouteEntity route3 = createTestRoute(hubA, hubD, 200.0, 120, RouteType.H2H);
        hubRouteRepository.saveAll(List.of(route1, route2, route3));

        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "estimatedDistanceKm"));

        // when
        Page<HubRouteEntity> result = hubRouteRepository.searchHubRoutes(
                hubA.getHubId(), null, RouteType.H2H, pageable
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getEstimatedDistanceKm()).isEqualByComparingTo("200.0");
        assertThat(result.getContent().get(1).getEstimatedDistanceKm()).isEqualByComparingTo("100.0");
    }

    @Test
    @DisplayName("성공: 특정 허브가 출발지이거나 도착지인 관련된 모든 경로를 조회한다")
    void findByInvolvedHubId_Success() {
        // given
        HubRouteEntity routeAtoB = createTestRoute(hubA, hubB, 10.0, 10, RouteType.H2H);
        HubRouteEntity routeBtoC = createTestRoute(hubB, hubC, 20.0, 20, RouteType.H2H);
        HubRouteEntity routeCtoD = createTestRoute(hubC, hubD, 30.0, 30, RouteType.H2H); // B와 무관
        hubRouteRepository.saveAll(List.of(routeAtoB, routeBtoC, routeCtoD));

        // when
        List<HubRouteEntity> result = hubRouteRepository.findByInvolvedHubId(hubB.getHubId());

        // then
        assertThat(result).hasSize(2); // A->B 와 B->C 만 검색되어야 함
        assertThat(result).extracting(HubRouteEntity::getEstimatedDistanceKm)
                .containsExactlyInAnyOrder(new BigDecimal("10.0"), new BigDecimal("20.0"));
    }

    @Test
    @DisplayName("성공: 200km 미만, 1.5배 거리 제한을 만족하는 최단 경유 경로(Inner Join)를 조회한다")
    void findOptimalTransitRoute_Success() {
        // given
        BigDecimal directDistance = new BigDecimal("150.0");

        // 최적 경로
        HubRouteEntity routeAtoB = createTestRoute(hubA, hubB, 70.0, 60, RouteType.H2H);
        HubRouteEntity routeBtoC = createTestRoute(hubB, hubC, 80.0, 60, RouteType.H2H);

        // 거리가 더 먼 경로
        HubRouteEntity routeAtoD = createTestRoute(hubA, hubD, 100.0, 60, RouteType.H2H);
        HubRouteEntity routeDtoC = createTestRoute(hubD, hubC, 100.0, 60, RouteType.H2H);

        // 1.5배 초과 경로
        HubRouteEntity routeAtoE = createTestRoute(hubA, hubE, 120.0, 60, RouteType.H2H);
        HubRouteEntity routeEtoC = createTestRoute(hubE, hubC, 120.0, 60, RouteType.H2H);

        // 200km 초과
        HubRouteEntity routeInvalid1 = createTestRoute(hubA, hubC, 210.0, 60, RouteType.H2H);

        hubRouteRepository.saveAll(List.of(
                routeAtoB, routeBtoC, routeAtoD, routeDtoC, routeAtoE, routeEtoC, routeInvalid1
        ));

        // when
        List<HubRouteEntity> result = hubRouteRepository.findOptimalTransitRoute(
                hubA.getHubId(), hubC.getHubId(), directDistance
        );

        // then
        assertThat(result).hasSize(2);

        assertThat(result.get(0).getDepartureHub().getHubId()).isEqualTo(hubA.getHubId());
        assertThat(result.get(0).getArrivalHub().getHubId()).isEqualTo(hubB.getHubId());
        assertThat(result.get(0).getEstimatedDistanceKm()).isEqualByComparingTo("70.0");

        assertThat(result.get(1).getDepartureHub().getHubId()).isEqualTo(hubB.getHubId());
        assertThat(result.get(1).getArrivalHub().getHubId()).isEqualTo(hubC.getHubId());
        assertThat(result.get(1).getEstimatedDistanceKm()).isEqualByComparingTo("80.0");
    }
}