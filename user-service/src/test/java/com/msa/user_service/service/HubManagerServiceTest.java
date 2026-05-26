package com.msa.user_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.client.HubClient;
import com.msa.user_service.dto.HubExistsResponse;
import com.msa.user_service.dto.HubManagerRequest;
import com.msa.user_service.dto.HubManagerResponse;
import com.msa.user_service.entity.HubManager;
import com.msa.user_service.entity.User;
import com.msa.user_service.fixture.TestFixtures;
import com.msa.user_service.global.UserErrorCode;
import com.msa.user_service.repository.HubManagerRepository;
import com.msa.user_service.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("HubManagerService 테스트")
class HubManagerServiceTest {

    @Mock private HubManagerRepository hubManagerRepository;
    @Mock private UserRepository userRepository;
    @Mock private HubClient hubClient;

    @InjectMocks
    private HubManagerService hubManagerService;

    @Test
    @DisplayName("허브 존재 확인 - 없으면 예외")
    void validateHubExists_hubNotFound() {
        // given
        HubExistsResponse notExists = hubExistsResponse(false);
        given(hubClient.checkHubExists(TestFixtures.HUB_ID)).willReturn(notExists);

        // then
        assertThatThrownBy(() -> hubManagerService.validateHubExists(TestFixtures.HUB_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.HUB_NOT_FOUND));
    }

    @Test
    @DisplayName("허브 매니저 등록 성공")
    void register_success() {
        // given
        HubManagerRequest request = hubManagerRequest(TestFixtures.USER_ID, TestFixtures.HUB_ID);
        HubManager saved = TestFixtures.hubManager();

        given(hubClient.checkHubExists(TestFixtures.HUB_ID)).willReturn(hubExistsResponse(true));
        given(hubManagerRepository.save(any(HubManager.class))).willReturn(saved);

        // when
        HubManagerResponse response = hubManagerService.register(request);

        // then
        assertThat(response.getHubManagerId()).isEqualTo(TestFixtures.HUB_MANAGER_ID);
        assertThat(response.getUserId()).isEqualTo(TestFixtures.USER_ID);
        assertThat(response.getHubId()).isEqualTo(TestFixtures.HUB_ID);
    }

    @Test
    @DisplayName("허브 매니저 등록 실패 - 허브 없음")
    void register_hubNotFound() {
        // given
        HubManagerRequest request = hubManagerRequest(TestFixtures.USER_ID, TestFixtures.HUB_ID);
        given(hubClient.checkHubExists(TestFixtures.HUB_ID)).willReturn(hubExistsResponse(false));

        // then
        assertThatThrownBy(() -> hubManagerService.register(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.HUB_NOT_FOUND));
    }


    @Test
    @DisplayName("허브 매니저 목록 조회 - 전체")
    void getList_noFilter() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        given(hubManagerRepository.findAllByDeletedAtIsNull(pageable))
                .willReturn(new PageImpl<>(List.of(TestFixtures.hubManager())));

        // when
        PageRes<HubManagerResponse> result = hubManagerService.getList(null, pageable);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("허브 매니저 목록 조회 - 허브 ID 필터")
    void getList_withHubFilter() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        given(hubManagerRepository.findAllByHubIdAndDeletedAtIsNull(TestFixtures.HUB_ID, pageable))
                .willReturn(new PageImpl<>(List.of(TestFixtures.hubManager())));

        // when
        PageRes<HubManagerResponse> result = hubManagerService.getList(TestFixtures.HUB_ID, pageable);

        // then
        assertThat(result).isNotNull();
        then(hubManagerRepository).should().findAllByHubIdAndDeletedAtIsNull(TestFixtures.HUB_ID, pageable);
    }

    @Test
    @DisplayName("허브 매니저 단건 조회 성공")
    void getOne_success() {
        // given
        HubManager hm = TestFixtures.hubManager();
        given(hubManagerRepository.findByHubManagerIdAndDeletedAtIsNull(TestFixtures.HUB_MANAGER_ID))
                .willReturn(Optional.of(hm));

        // when
        HubManagerResponse response = hubManagerService.getOne(TestFixtures.HUB_MANAGER_ID);

        // then
        assertThat(response.getHubManagerId()).isEqualTo(TestFixtures.HUB_MANAGER_ID);
    }

    @Test
    @DisplayName("허브 매니저 단건 조회 실패 - 없는 ID")
    void getOne_notFound() {
        // given
        given(hubManagerRepository.findByHubManagerIdAndDeletedAtIsNull(any()))
                .willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> hubManagerService.getOne(UUID.randomUUID()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.HUB_MANAGER_NOT_FOUND));
    }

    @Test
    @DisplayName("허브 매니저 삭제 성공")
    void delete_success() {
        // given
        HubManager hm = TestFixtures.hubManager();
        given(hubManagerRepository.findByHubManagerIdAndDeletedAtIsNull(TestFixtures.HUB_MANAGER_ID))
                .willReturn(Optional.of(hm));

        // when
        hubManagerService.delete(TestFixtures.HUB_MANAGER_ID, "admin");

        // then: soft delete 확인
        assertThat(hm.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("허브 매니저 삭제 실패 - 없는 ID")
    void delete_notFound() {
        // given
        given(hubManagerRepository.findByHubManagerIdAndDeletedAtIsNull(any()))
                .willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> hubManagerService.delete(UUID.randomUUID(), "admin"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.HUB_MANAGER_NOT_FOUND));
    }

    @Test
    @DisplayName("허브 소속 여부 확인")
    void existsByUserIdAndHubId() {
        // given
        given(hubManagerRepository.existsByUserIdAndHubIdAndDeletedAtIsNull(TestFixtures.USER_ID, TestFixtures.HUB_ID))
                .willReturn(true);

        // when & then
        assertThat(hubManagerService.existsByUserIdAndHubId(TestFixtures.USER_ID, TestFixtures.HUB_ID)).isTrue();
    }

    @Test
    @DisplayName("Internal: 허브 ID로 허브매니저 정보 조회")
    void getHubManagerByHubIdForInternal_success() {
        // given
        HubManager hm = TestFixtures.hubManager();
        User user = TestFixtures.approvedMasterUser();

        given(hubManagerRepository.findByHubIdAndDeletedAtIsNull(TestFixtures.HUB_ID))
                .willReturn(Optional.of(hm));
        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(user));

        // when & then
        assertThat(hubManagerService.getHubManagerByHubIdForInternal(TestFixtures.HUB_ID)).isNotNull();
    }

    private HubManagerRequest hubManagerRequest(UUID userId, UUID hubId) {
        HubManagerRequest req = new HubManagerRequest();
        ReflectionTestUtils.setField(req, "userId", userId);
        ReflectionTestUtils.setField(req, "hubId", hubId);
        return req;
    }

    private HubExistsResponse hubExistsResponse(boolean exists) {
        HubExistsResponse res = new HubExistsResponse();
        ReflectionTestUtils.setField(res, "exists", exists);
        return res;
    }
}
