package com.msa.hub_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.hub_service.client.AddressGeocodingPort;
import com.msa.hub_service.client.CompanyClient;
import com.msa.hub_service.dto.CompanyDto;
import com.msa.hub_service.dto.CompanyNameResponse;
import com.msa.hub_service.dto.HubRouteResponse;
import com.msa.hub_service.dto.HubRouteUpdateRequest;
import com.msa.hub_service.entity.RouteInfo;
import com.msa.hub_service.entity.HubEntity;
import com.msa.hub_service.entity.HubRouteEntity;
import com.msa.hub_service.entity.RouteType;
import com.msa.hub_service.global.HubErrorCode;
import com.msa.hub_service.global.Util;
import com.msa.hub_service.message.HubCreatedEvent;
import com.msa.hub_service.repository.HubRepository;
import com.msa.hub_service.repository.HubRouteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HubRouteServiceTest {

    @Mock
    private HubRouteRepository hubRouteRepository;
    @Mock
    private HubRepository hubRepository;
    @Mock
    private AuditorAware<String> auditorAware;
    @Mock
    private CompanyClient companyClient;
    @Mock
    private AddressGeocodingPort geocodingPort;

    @InjectMocks
    private HubRouteService hubRouteService;

    // Hub Mock
    private HubEntity createHub(UUID id, String name, BigDecimal lat, BigDecimal lon) {
        HubEntity hub = HubEntity.create(name, "테스트 주소", lat, lon);
        ReflectionTestUtils.setField(hub, "hubId", id);
        return hub;
    }

    // Route Mock
    private HubRouteEntity createRoute(UUID id, HubEntity dep, HubEntity arr, BigDecimal km, Integer min, RouteType type) {
        HubRouteEntity route = HubRouteEntity.create(dep, arr);
        ReflectionTestUtils.setField(route, "hubRouteId", id);
        ReflectionTestUtils.setField(route, "estimatedDistanceKm", km);
        ReflectionTestUtils.setField(route, "estimatedDurationMin", min);
        ReflectionTestUtils.setField(route, "routeType", type);
        return route;
    }

    @Nested
    @DisplayName("허브 경로 생성 테스트")
    class CreateHubRouteTest {

        @Test
        @DisplayName("성공: 올바른 정보가 주어지면 두 허브 간의 경로를 생성한다")
        void createHubRoute_Success() {
            // given
            UUID depId = UUID.randomUUID();
            UUID arrId = UUID.randomUUID();
            HubEntity depHub = createHub(depId, "서울 허브", new BigDecimal("37.5"), new BigDecimal("126.5"));
            HubEntity arrHub = createHub(arrId, "부산 허브", new BigDecimal("35.1"), new BigDecimal("129.0"));

            when(hubRouteRepository.existsByDepartureHub_HubIdAndArrivalHub_HubId(depId, arrId)).thenReturn(false);
            when(hubRepository.findById(depId)).thenReturn(Optional.of(depHub));
            when(hubRepository.findById(arrId)).thenReturn(Optional.of(arrHub));

            HubRouteEntity mockRoute = createRoute(UUID.randomUUID(), depHub, arrHub, BigDecimal.valueOf(320.5), 240, RouteType.H2H);
            when(hubRouteRepository.save(any(HubRouteEntity.class))).thenReturn(mockRoute);

            // when
            HubRouteResponse response = hubRouteService.createHubRoute(depId, arrId);

            // then
            assertThat(response).isNotNull();
            verify(hubRouteRepository, times(1)).save(any(HubRouteEntity.class));
        }

        @Test
        @DisplayName("실패: 출발지와 도착지 허브 ID가 같으면 SAME_HUB_NOT_ALLOWED 예외가 발생한다")
        void createHubRoute_Throws_SameHub() {
            // given
            UUID sameId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> hubRouteService.createHubRoute(sameId, sameId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(HubErrorCode.SAME_HUB_NOT_ALLOWED.getMessage());
        }

        @Test
        @DisplayName("실패: 허브 좌표 정보가 유실(null)되어 있으면 NULL_COORDINATES 예외가 발생한다")
        void createHubRoute_Throws_NullCoordinates() {
            // given
            UUID depId = UUID.randomUUID();
            UUID arrId = UUID.randomUUID();
            HubEntity depHub = createHub(depId, "출발 허브", null, new BigDecimal("126.5"));
            HubEntity arrHub = createHub(arrId, "도착 허브", new BigDecimal("35.1"), new BigDecimal("129.0"));

            when(hubRouteRepository.existsByDepartureHub_HubIdAndArrivalHub_HubId(depId, arrId)).thenReturn(false);
            when(hubRepository.findById(depId)).thenReturn(Optional.of(depHub));
            when(hubRepository.findById(arrId)).thenReturn(Optional.of(arrHub));

            // when & then
            assertThatThrownBy(() -> hubRouteService.createHubRoute(depId, arrId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(HubErrorCode.NULL_COORDINATES.getMessage());
        }
    }

    @Nested
    @DisplayName("허브 경로 수정 테스트")
    class UpdateHubRouteTest {

        @Test
        @DisplayName("성공: 거리(km)와 시간(min)이 모두 변경되면 Util을 활용해 RouteType(H2H)을 올바르게 갱신한다")
        void updateHubRoute_BothChanged() {
            // given
            UUID routeId = UUID.randomUUID();
            HubEntity dep = createHub(UUID.randomUUID(), "출발", new BigDecimal("37"), new BigDecimal("126"));
            HubEntity arr = createHub(UUID.randomUUID(), "도착", new BigDecimal("38"), new BigDecimal("127"));
            HubRouteEntity originalRoute = createRoute(routeId, dep, arr, BigDecimal.valueOf(50), 30, RouteType.P2P);

            HubRouteUpdateRequest request = new HubRouteUpdateRequest(BigDecimal.valueOf(250), 150);

            when(hubRouteRepository.findById(routeId)).thenReturn(Optional.of(originalRoute));

            try (MockedStatic<Util.RouteCalculator> mockedCalculator = mockStatic(Util.RouteCalculator.class)) {
                mockedCalculator.when(() -> Util.RouteCalculator.determineRouteType(250.0)).thenReturn(RouteType.H2H);

                // when
                HubRouteResponse response = hubRouteService.updateHubRoute(routeId, request);

                // then
                assertThat(response.estimatedDistanceKm()).isEqualTo(BigDecimal.valueOf(250));
                assertThat(response.estimatedDurationMin()).isEqualTo(150);
                assertThat(response.routeType()).isEqualTo(RouteType.H2H);
            }
        }
    }

    @Nested
    @DisplayName("업체 간 경로 조회 테스트")
    class CompanyToCompanyPathTest {

        @Test
        @DisplayName("성공: 경로 리스트를 반환한다")
        void getCompanyToCompanyPath_Success() {
            // given
            UUID depCompanyId = UUID.randomUUID();
            UUID arrCompanyId = UUID.randomUUID();
            UUID depHubId = UUID.randomUUID();
            UUID arrHubId = UUID.randomUUID();

            CompanyDto depCompany = new CompanyDto("출발지 주소", new BigDecimal("37.1"), new BigDecimal("126.1"), depHubId);
            CompanyDto arrCompany = new CompanyDto("도착지 주소", new BigDecimal("37.9"), new BigDecimal("126.9"), null);

            HubEntity depHub = createHub(depHubId, "출발허브", new BigDecimal("37"), new BigDecimal("126"));
            HubEntity closeHub = createHub(arrHubId, "가장 가까운 허브", new BigDecimal("37.85"), new BigDecimal("126.85"));
            HubEntity farHub = createHub(UUID.randomUUID(), "멀리 있는 허브", new BigDecimal("35.0"), new BigDecimal("129.0"));

            HubRouteEntity mockDirectRoute = createRoute(
                    UUID.randomUUID(),
                    depHub,
                    closeHub,
                    BigDecimal.valueOf(60), 45, RouteType.P2P
            );

            String expectedCompanyName = "도착 테스트 업체";
            when(companyClient.getCompanyNames(List.of(arrCompanyId)))
                    .thenReturn(List.of(new CompanyNameResponse(arrCompanyId, expectedCompanyName)));

            when(companyClient.getCompanyLocation(depCompanyId)).thenReturn(depCompany);
            when(companyClient.getCompanyLocation(arrCompanyId)).thenReturn(arrCompany);
            when(hubRepository.findAll()).thenReturn(List.of(depHub, farHub, closeHub));
            when(hubRouteRepository.findByDepartureHub_HubIdAndArrivalHub_HubId(any(UUID.class), any(UUID.class)))
                    .thenReturn(Optional.of(mockDirectRoute));

            try (MockedStatic<Util.DistanceCalculator> mockedDistance = mockStatic(Util.DistanceCalculator.class);
                 MockedStatic<Util.RouteCalculator> mockedRoute = mockStatic(Util.RouteCalculator.class)) {

                mockedDistance.when(() -> Util.DistanceCalculator.getDistance(farHub.getLatitude(), farHub.getLongitude(), arrCompany.latitude(), arrCompany.longitude())).thenReturn(450.0);
                mockedDistance.when(() -> Util.DistanceCalculator.getDistance(closeHub.getLatitude(), closeHub.getLongitude(), arrCompany.latitude(), arrCompany.longitude())).thenReturn(8.5);

                RouteInfo mockCalcResult = new RouteInfo(BigDecimal.valueOf(11.2), 15, RouteType.P2P);
                mockedRoute.when(() -> Util.RouteCalculator.calculate(any(), any(), any(), any())).thenReturn(mockCalcResult);

                // when
                List<HubRouteResponse> result = hubRouteService.getCompanyToCompanyPath(depCompanyId, arrCompanyId);

                // then
                assertThat(result).hasSize(2);

                // 첫 번째 경로 (허브 -> 허브) 검증
                assertThat(result.get(0).departureHub()).isEqualTo(depHubId);
                assertThat(result.get(0).departureHubName()).isEqualTo("출발허브");
                assertThat(result.get(0).arrivalHubName()).isEqualTo("가장 가까운 허브");

                // 두 번째 경로 (마지막 허브 -> 도착 업체) 검증
                assertThat(result.get(1).arrivalCompanyId()).isEqualTo(arrCompanyId);
                assertThat(result.get(1).arrivalCompanyName()).isEqualTo(expectedCompanyName);
                assertThat(result.get(1).estimatedDistanceKm()).isEqualTo(BigDecimal.valueOf(11.2));
            }
        }
    }

    @Nested
    @DisplayName("이벤트 리스너 테스트 (비동기 생성)")
    class EventListenerTest {

        @Test
        @DisplayName("성공: 신규 허브 이벤트를 받으면 ArgumentCaptor를 사용해 Iterable 내부 개수를 안전하게 검증한다")
        @SuppressWarnings("unchecked")
        void createRoutesForNewHub_Success() {
            // given
            UUID newHubId = UUID.randomUUID();
            UUID existHubId = UUID.randomUUID();

            HubEntity newHub = createHub(newHubId, "제주 허브", new BigDecimal("33.4"), new BigDecimal("126.5"));
            HubEntity existHub = createHub(existHubId, "목포 허브", new BigDecimal("34.8"), new BigDecimal("126.3"));

            HubCreatedEvent event = new HubCreatedEvent(newHubId);

            when(hubRepository.findById(newHubId)).thenReturn(Optional.of(newHub));
            when(hubRepository.findByHubIdNot(newHubId)).thenReturn(List.of(existHub));
            when(hubRouteRepository.findByInvolvedHubId(newHubId)).thenReturn(List.of());

            // when
            hubRouteService.createRoutesForNewHub(event);

            // then
            ArgumentCaptor<Iterable<HubRouteEntity>> routeCaptor = ArgumentCaptor.forClass(Iterable.class);
            verify(hubRouteRepository, times(1)).saveAll(routeCaptor.capture());
            assertThat(routeCaptor.getValue()).hasSize(2);
        }
    }
}