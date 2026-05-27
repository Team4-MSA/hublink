package com.msa.hub_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.hub_service.client.AddressGeocodingPort;
import com.msa.hub_service.dto.CoordinateDto;
import com.msa.hub_service.dto.HubRequest;
import com.msa.hub_service.dto.HubResponse;
import com.msa.hub_service.entity.HubEntity;
import com.msa.hub_service.global.HubErrorCode;
import com.msa.hub_service.message.HubCreatedEvent;
import com.msa.hub_service.message.HubDeletedEvent;
import com.msa.hub_service.message.HubUpdatedEvent;
import com.msa.hub_service.repository.HubRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
class HubServiceTest {

    @Mock
    private HubRepository hubRepository;
    @Mock
    private AddressGeocodingPort geocodingPort;
    @Mock
    private AuditorAware<String> auditorAware;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private HubService hubService;

    // 허브 Mock
    private HubEntity createMockHub(UUID id, String name, String address, BigDecimal lat, BigDecimal lon) {
        HubEntity hub = HubEntity.create(name, address, lat, lon);
        ReflectionTestUtils.setField(hub, "hubId", id);
        return hub;
    }

    @Nested
    @DisplayName("허브 생성 테스트")
    class CreateHubTest {

        @Test
        @DisplayName("성공: 좌표가 입력되지 않으면 API를 호출하여 허브를 생성하고 이벤트를 발행")
        void createHub_WithApiCall_Success() {
            // given
            String name = "서울 허브";
            String address = "서울특별시 중구";
            UUID mockId = UUID.randomUUID();
            CoordinateDto coordinate = new CoordinateDto(new BigDecimal("37.5665"), new BigDecimal("126.9780"));

            when(hubRepository.existsByName(name)).thenReturn(false);
            when(hubRepository.existsByAddress(address)).thenReturn(false);
            when(geocodingPort.getCoordinate(address)).thenReturn(coordinate);

            doAnswer(invocation -> {
                HubEntity savedHub = invocation.getArgument(0);
                ReflectionTestUtils.setField(savedHub, "hubId", mockId);
                return savedHub;
            }).when(hubRepository).save(any(HubEntity.class));

            // when
            HubResponse response = hubService.createHub(name, address, null, null);

            // then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo(name);
            verify(geocodingPort, times(1)).getCoordinate(address); // API 호출
            verify(hubRepository, times(1)).save(any(HubEntity.class));
            verify(eventPublisher, times(1)).publishEvent(any(HubCreatedEvent.class));
        }

        @Test
        @DisplayName("성공: 좌표가 직접 입력되면 API를 호출하지 않고 허브를 생성하고 이벤트를 발행")
        void createHub_WithoutApiCall_Success() {
            // given
            String name = "부산 허브";
            String address = "부산광역시 해운대구";
            BigDecimal inputLat = new BigDecimal("35.1595");
            BigDecimal inputLon = new BigDecimal("129.1625");
            UUID mockId = UUID.randomUUID();

            when(hubRepository.existsByName(name)).thenReturn(false);
            when(hubRepository.existsByAddress(address)).thenReturn(false);

            doAnswer(invocation -> {
                HubEntity savedHub = invocation.getArgument(0);
                ReflectionTestUtils.setField(savedHub, "hubId", mockId);
                return savedHub;
            }).when(hubRepository).save(any(HubEntity.class));

            // when
            HubResponse response = hubService.createHub(name, address, inputLat, inputLon);

            // then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo(name);
            verify(geocodingPort, never()).getCoordinate(anyString()); // API 미호출
            verify(hubRepository, times(1)).save(any(HubEntity.class));
            verify(eventPublisher, times(1)).publishEvent(any(HubCreatedEvent.class));
        }

        @Test
        @DisplayName("실패: 이름이 중복되면 HUB_NAME_DUPLICATED 예외가 발생")
        void createHub_Throws_DuplicateName() {
            // given
            when(hubRepository.existsByName("중복이름")).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> hubService.createHub("중복이름", "주소", null, null))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(HubErrorCode.HUB_NAME_DUPLICATED.getMessage());
        }
    }

    @Nested
    @DisplayName("허브 상세 조회 테스트")
    class GetHubTest {

        @Test
        @DisplayName("성공: 존재하는 허브 ID로 조회 시 응답을 반환")
        void getHub_Success() {
            // given
            UUID hubId = UUID.randomUUID();
            HubEntity hub = createMockHub(hubId, "경기 허브", "경기도 수원시", null, null);
            when(hubRepository.findById(hubId)).thenReturn(Optional.of(hub));

            // when
            HubResponse response = hubService.getHub(hubId);

            // then
            assertThat(response.hubId()).isEqualTo(hubId);
            assertThat(response.name()).isEqualTo("경기 허브");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 허브 ID 조회 시 HUB_NOT_FOUND 예외가 발생")
        void getHub_NotFound() {
            // given
            UUID hubId = UUID.randomUUID();
            when(hubRepository.findById(hubId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hubService.getHub(hubId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(HubErrorCode.HUB_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("허브 수정 테스트")
    class UpdateHubTest {

        @Test
        @DisplayName("성공: 주소가 변경되고 좌표 미입력 시 API를 호출하여 이벤트를 발행")
        void updateHub_CoordinateChanged_WithApiCall_PublishesEvent() {
            // given
            UUID hubId = UUID.randomUUID();
            HubEntity originalHub = createMockHub(hubId, "기존 허브", "기존 주소", new BigDecimal("37.1"), new BigDecimal("126.1"));
            HubRequest request = new HubRequest("새로운 허브", "새로운 주소", null, null); // API 호출

            CoordinateDto newCoordinate = new CoordinateDto(new BigDecimal("37.5"), new BigDecimal("126.5"));

            when(hubRepository.findById(hubId)).thenReturn(Optional.of(originalHub));
            when(hubRepository.existsByName(request.name())).thenReturn(false);
            when(hubRepository.existsByAddress(request.address())).thenReturn(false);
            when(geocodingPort.getCoordinate(request.address())).thenReturn(newCoordinate);

            // when
            HubResponse response = hubService.updateHub(hubId, request);

            // then
            assertThat(response.name()).isEqualTo("새로운 허브");
            assertThat(response.address()).isEqualTo("새로운 주소");
            verify(geocodingPort, times(1)).getCoordinate(request.address()); // API 호출
            verify(eventPublisher, times(1)).publishEvent(any(HubUpdatedEvent.class));
        }

        @Test
        @DisplayName("성공: 주소가 변경되고 좌표 직접 입력 시 API 미호출 후 이벤트 발행")
        void updateHub_CoordinateChanged_WithoutApiCall_PublishesEvent() {
            // given
            UUID hubId = UUID.randomUUID();
            HubEntity originalHub = createMockHub(hubId, "기존 허브", "기존 주소", new BigDecimal("37.1"), new BigDecimal("126.1"));
            BigDecimal newLat = new BigDecimal("37.6");
            BigDecimal newLon = new BigDecimal("126.6");
            HubRequest request = new HubRequest("새로운 허브", "새로운 주소", newLat, newLon);

            when(hubRepository.findById(hubId)).thenReturn(Optional.of(originalHub));
            when(hubRepository.existsByName(request.name())).thenReturn(false);
            when(hubRepository.existsByAddress(request.address())).thenReturn(false);

            // when
            HubResponse response = hubService.updateHub(hubId, request);

            // then
            assertThat(response.name()).isEqualTo("새로운 허브");
            assertThat(response.address()).isEqualTo("새로운 주소");
            verify(eventPublisher, times(1)).publishEvent(any(HubUpdatedEvent.class));
        }
    }

    @Nested
    @DisplayName("허브 삭제 테스트")
    class DeleteHubTest {

        @Test
        @DisplayName("성공: 허브를 소프트 딜리트하고 삭제 이벤트를 발행")
        void deleteHub_Success() {
            // given
            UUID hubId = UUID.randomUUID();
            HubEntity hub = createMockHub(hubId, "삭제될 허브", "주소", null, null);

            when(hubRepository.findById(hubId)).thenReturn(Optional.of(hub));
            when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("TEST_USER"));

            // when
            HubResponse response = hubService.deleteHub(hubId);

            // then
            assertThat(response).isNotNull();
            // 이벤트 실행 확인 - 횟수1
            verify(eventPublisher, times(1)).publishEvent(any(HubDeletedEvent.class));
        }
    }

    @Nested
    @DisplayName("허브 목록 검색 테스트")
    class GetHubsTest {

        @Test
        @DisplayName("성공: 키워드가 있을 때 키워드 포함 검색 쿼리를 실행")
        void getHubs_With_Keyword() {
            // given
            String keyword = "서울";
            PageRequest pageable = PageRequest.of(0, 10);
            HubEntity hub = createMockHub(UUID.randomUUID(), "서울 허브", "주소", null, null);
            Page<HubEntity> page = new PageImpl<>(List.of(hub), pageable, 1);

            when(hubRepository.findByNameContaining(keyword, pageable)).thenReturn(page);

            // when
            PageRes<HubResponse> result = hubService.getHubs(keyword, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("서울 허브");
            verify(hubRepository, times(1)).findByNameContaining(keyword, pageable);
        }
    }
}