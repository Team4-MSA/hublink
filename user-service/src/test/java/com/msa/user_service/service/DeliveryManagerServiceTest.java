package com.msa.user_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.user_service.client.HubClient;
import com.msa.user_service.dto.DeliveryManagerRequest;
import com.msa.user_service.dto.DeliveryManagerResponse;
import com.msa.user_service.dto.HubExistsResponse;
import com.msa.user_service.dto.InternalDeliveryManagerResponse;
import com.msa.user_service.dto.UpdateDeliveryManagerRequest;
import com.msa.user_service.entity.DeliveryManager;
import com.msa.user_service.entity.DeliveryManagerType;
import com.msa.user_service.entity.User;
import com.msa.user_service.fixture.TestFixtures;
import com.msa.user_service.global.UserErrorCode;
import com.msa.user_service.repository.DeliveryManagerRepository;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryManagerService 테스트")
class DeliveryManagerServiceTest {

    @Mock private DeliveryManagerRepository deliveryManagerRepository;
    @Mock private UserRepository userRepository;
    @Mock private HubClient hubClient;

    @InjectMocks
    private DeliveryManagerService deliveryManagerService;

    @Test
    @DisplayName("배송 담당자 등록 성공 - MASTER 권한")
    void register_success_asMaster() {
        // given
        DeliveryManagerRequest request = dmRequest(TestFixtures.USER_ID, TestFixtures.HUB_ID, DeliveryManagerType.HUB_DELIVERY);
        User user = TestFixtures.approvedMasterUser();
        DeliveryManager saved = TestFixtures.hubDeliveryManager();

        given(hubClient.checkHubExists(TestFixtures.HUB_ID)).willReturn(hubExistsResponse(true));
        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID)).willReturn(Optional.of(user));
        given(deliveryManagerRepository.findLatestByHubId(TestFixtures.HUB_ID)).willReturn(Optional.empty());
        given(deliveryManagerRepository.save(any(DeliveryManager.class))).willReturn(saved);

        // when
        DeliveryManagerResponse response = deliveryManagerService.register(request, "MASTER", TestFixtures.ADMIN_ID);

        // then
        assertThat(response.getUserId()).isEqualTo(TestFixtures.USER_ID);
        assertThat(response.getType()).isEqualTo(DeliveryManagerType.HUB_DELIVERY);
    }

    @Test
    @DisplayName("배송 담당자 등록 성공 - HUB_MANAGER: 담당 허브 접근 허용")
    void register_success_asHubManager() {
        // given
        DeliveryManagerRequest request = dmRequest(TestFixtures.USER_ID, TestFixtures.HUB_ID, DeliveryManagerType.HUB_DELIVERY);
        User hubManagerUser = TestFixtures.approvedHubManagerUser(); // ADMIN_ID, HUB_ID 소속
        User targetUser = TestFixtures.approvedMasterUser();
        DeliveryManager saved = TestFixtures.hubDeliveryManager();

        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.ADMIN_ID)).willReturn(Optional.of(hubManagerUser));
        given(hubClient.checkHubExists(TestFixtures.HUB_ID)).willReturn(hubExistsResponse(true));
        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID)).willReturn(Optional.of(targetUser));
        given(deliveryManagerRepository.findLatestByHubId(TestFixtures.HUB_ID)).willReturn(Optional.empty());
        given(deliveryManagerRepository.save(any(DeliveryManager.class))).willReturn(saved);

        // when
        DeliveryManagerResponse response = deliveryManagerService.register(request, "HUB_MANAGER", TestFixtures.ADMIN_ID);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("배송 담당자 등록 실패 - HUB_MANAGER: 담당 허브 아닌 경우")
    void register_hubAccessDenied() {
        // given
        UUID otherHubId = UUID.randomUUID();
        DeliveryManagerRequest otherHubRequest = dmRequest(TestFixtures.USER_ID, otherHubId, DeliveryManagerType.HUB_DELIVERY);
        User hubManagerUser = TestFixtures.approvedHubManagerUser(); // HUB_ID 소속

        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.ADMIN_ID)).willReturn(Optional.of(hubManagerUser));

        // then
        assertThatThrownBy(() -> deliveryManagerService.register(otherHubRequest, "HUB_MANAGER", TestFixtures.ADMIN_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.HUB_ACCESS_DENIED));
    }

    @Test
    @DisplayName("배송 담당자 등록 실패 - 유저 없음")
    void register_userNotFound() {
        // given
        DeliveryManagerRequest request = dmRequest(TestFixtures.USER_ID, TestFixtures.HUB_ID, DeliveryManagerType.HUB_DELIVERY);

        given(hubClient.checkHubExists(TestFixtures.HUB_ID)).willReturn(hubExistsResponse(true));
        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> deliveryManagerService.register(request, "MASTER", TestFixtures.ADMIN_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("배송 담당자 단건 조회 성공 - MASTER")
    void getOne_asMaster() {
        // given
        DeliveryManager dm = TestFixtures.hubDeliveryManager();
        given(deliveryManagerRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(dm));

        // when
        DeliveryManagerResponse response = deliveryManagerService.getOne(
                TestFixtures.USER_ID, "MASTER", TestFixtures.ADMIN_ID);

        // then
        assertThat(response.getUserId()).isEqualTo(TestFixtures.USER_ID);
    }

    @Test
    @DisplayName("배송 담당자 단건 조회 - DELIVERY_MANAGER: 본인 외 접근 불가")
    void getOne_deliveryManager_selfOnly() {
        // given
        DeliveryManager dm = TestFixtures.hubDeliveryManager(); // userId = USER_ID
        UUID otherUserId = UUID.randomUUID();

        given(deliveryManagerRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(dm));

        // then
        assertThatThrownBy(() ->
                deliveryManagerService.getOne(TestFixtures.USER_ID, "DELIVERY_MANAGER", otherUserId))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.SELF_ONLY_ACCESS));
    }

    @Test
    @DisplayName("배송 담당자 단건 조회 실패 - 없는 ID")
    void getOne_notFound() {
        // given
        given(deliveryManagerRepository.findByUserIdAndDeletedAtIsNull(any()))
                .willReturn(Optional.empty());

        // then
        assertThatThrownBy(() ->
                deliveryManagerService.getOne(UUID.randomUUID(), "MASTER", TestFixtures.ADMIN_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.DELIVERY_MANAGER_NOT_FOUND));
    }

    @Test
    @DisplayName("배송 담당자 목록 조회 - MASTER: 전체 조회")
    void getList_asMaster_noFilter() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        given(deliveryManagerRepository.findAllByDeletedAtIsNull(pageable))
                .willReturn(new PageImpl<>(List.of(TestFixtures.hubDeliveryManager())));

        // when
        PageRes<DeliveryManagerResponse> result = deliveryManagerService.getList(
                null, null, pageable, "MASTER", TestFixtures.ADMIN_ID);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("배송 담당자 삭제 성공 - MASTER")
    void delete_asMaster() {
        // given
        DeliveryManager dm = TestFixtures.hubDeliveryManager();
        given(deliveryManagerRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(dm));

        // when
        deliveryManagerService.delete(TestFixtures.USER_ID, "admin", "MASTER", TestFixtures.ADMIN_ID);

        // then
        assertThat(dm.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Internal: 허브 리스트별 배송 담당자 목록 조회")
    void getDeliveryManagersByHubsForInternal_success() {
        // given
        DeliveryManager dm = TestFixtures.hubDeliveryManager();
        User user = TestFixtures.approvedMasterUser();

        given(deliveryManagerRepository.findAllByHubIdInAndDeletedAtIsNull(List.of(TestFixtures.HUB_ID)))
                .willReturn(List.of(dm));
        given(userRepository.findAllByUserIdInAndDeletedAtIsNull(List.of(TestFixtures.USER_ID)))
                .willReturn(List.of(user));

        // when
        List<InternalDeliveryManagerResponse> result =
                deliveryManagerService.getDeliveryManagersByHubsForInternal(List.of(TestFixtures.HUB_ID));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeliveryManagerId()).isEqualTo(TestFixtures.USER_ID);
    }

    @Test
    @DisplayName("배송 담당자 수정 - type, slackId 변경 및 User.slackId 전파")
    void update_typeAndSlackId() {
        // given
        DeliveryManager dm = TestFixtures.hubDeliveryManager();
        User user = TestFixtures.pendingDeliveryManagerUser();
        UpdateDeliveryManagerRequest request = updateDmRequest(null, DeliveryManagerType.HUB_DELIVERY, "U_NEW_SLACK");

        given(deliveryManagerRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(dm));
        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(user));

        // when
        DeliveryManagerResponse response = deliveryManagerService.update(
                TestFixtures.USER_ID, request, "MASTER", TestFixtures.ADMIN_ID);

        // then
        assertThat(response.getSlackId()).isEqualTo("U_NEW_SLACK");
        assertThat(user.getSlackId()).isEqualTo("U_NEW_SLACK"); // User에 전파됐는지 확인
    }

    @Test
    @DisplayName("배송 담당자 수정 - hubId 변경 시 User.hubId 전파 (MASTER만 가능)")
    void update_hubId_asMaster() {
        // given
        UUID newHubId = UUID.randomUUID();
        DeliveryManager dm = TestFixtures.hubDeliveryManager(); // HUB_ID 소속
        User user = TestFixtures.pendingDeliveryManagerUser();
        UpdateDeliveryManagerRequest request = updateDmRequest(newHubId, null, null);

        given(deliveryManagerRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(dm));
        given(hubClient.checkHubExists(newHubId)).willReturn(hubExistsResponse(true));
        given(deliveryManagerRepository.findLatestByHubId(newHubId)).willReturn(Optional.empty());
        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(user));

        // when
        deliveryManagerService.update(TestFixtures.USER_ID, request, "MASTER", TestFixtures.ADMIN_ID);

        // then
        assertThat(dm.getHubId()).isEqualTo(newHubId);
        assertThat(user.getHubId()).isEqualTo(newHubId); // User에 전파됐는지 확인
    }

    @Test
    @DisplayName("배송 담당자 수정 - HUB_MANAGER가 hubId 변경 시도 → 403")
    void update_hubId_asHubManager_forbidden() {
        // given
        UUID newHubId = UUID.randomUUID();
        DeliveryManager dm = TestFixtures.hubDeliveryManager(); // HUB_ID 소속
        User hubManagerUser = TestFixtures.approvedHubManagerUser(); // HUB_ID 소속 허브 매니저
        UpdateDeliveryManagerRequest request = updateDmRequest(newHubId, null, null);

        given(deliveryManagerRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(dm));
        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.ADMIN_ID))
                .willReturn(Optional.of(hubManagerUser));

        // then
        assertThatThrownBy(() ->
                deliveryManagerService.update(TestFixtures.USER_ID, request, "HUB_MANAGER", TestFixtures.ADMIN_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.HUB_ACCESS_DENIED));
    }

    @Test
    @DisplayName("배송 담당자 수정 - HUB_MANAGER가 다른 허브 담당자 수정 시도 → 403")
    void update_wrongHub_asHubManager_forbidden() {
        // given
        DeliveryManager dm = TestFixtures.hubDeliveryManager(); // HUB_ID 소속
        UUID otherHubId = UUID.randomUUID();
        User otherHubManager = TestFixtures.approvedHubManagerUser();
        // otherHubManager의 hubId를 다른 값으로 세팅
        TestFixtures.setField(otherHubManager, "hubId", otherHubId);
        UpdateDeliveryManagerRequest request = updateDmRequest(null, DeliveryManagerType.HUB_DELIVERY, null);

        given(deliveryManagerRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(dm));
        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.ADMIN_ID))
                .willReturn(Optional.of(otherHubManager));

        // then
        assertThatThrownBy(() ->
                deliveryManagerService.update(TestFixtures.USER_ID, request, "HUB_MANAGER", TestFixtures.ADMIN_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.HUB_ACCESS_DENIED));
    }

    private DeliveryManagerRequest dmRequest(UUID userId, UUID hubId, DeliveryManagerType type) {
        DeliveryManagerRequest req = new DeliveryManagerRequest();
        ReflectionTestUtils.setField(req, "userId", userId);
        ReflectionTestUtils.setField(req, "hubId", hubId);
        ReflectionTestUtils.setField(req, "type", type);
        return req;
    }

    private UpdateDeliveryManagerRequest updateDmRequest(UUID hubId, DeliveryManagerType type, String slackId) {
        UpdateDeliveryManagerRequest req = new UpdateDeliveryManagerRequest();
        ReflectionTestUtils.setField(req, "hubId", hubId);
        ReflectionTestUtils.setField(req, "type", type);
        ReflectionTestUtils.setField(req, "slackId", slackId);
        return req;
    }

    private HubExistsResponse hubExistsResponse(boolean exists) {
        HubExistsResponse res = new HubExistsResponse();
        ReflectionTestUtils.setField(res, "exists", exists);
        return res;
    }
}
