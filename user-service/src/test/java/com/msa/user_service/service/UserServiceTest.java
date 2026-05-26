package com.msa.user_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.user_service.client.CompanyClient;
import com.msa.user_service.client.HubClient;
import com.msa.user_service.dto.ApproveUserRequest;
import com.msa.user_service.dto.HubExistsResponse;
import com.msa.user_service.dto.SignUpRequest;
import com.msa.user_service.dto.UpdateUserRequest;
import com.msa.user_service.dto.UserResponse;
import com.msa.user_service.entity.DeliveryManagerType;
import com.msa.user_service.entity.User;
import com.msa.user_service.entity.UserRole;
import com.msa.user_service.entity.UserStatus;
import com.msa.user_service.fixture.TestFixtures;
import com.msa.user_service.global.UserErrorCode;
import com.msa.user_service.repository.UserRepository;
import com.msa.user_service.util.RedisUtil;
import org.springframework.transaction.support.TransactionSynchronization;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserApprovalService userApprovalService;
    @Mock private DeliveryManagerService deliveryManagerService;
    @Mock private HubClient hubClient;
    @Mock private CompanyClient companyClient;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RedisUtil redisUtil;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() {
        // given
        SignUpRequest request = signUpRequest("newuser1", "NewPass1!", "새유저", "new@example.com");
        User savedUser = TestFixtures.approvedMasterUser();

        given(userRepository.existsByUsername("newuser1")).willReturn(false);
        given(userRepository.existsByEmail("new@example.com")).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encoded");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        UserResponse response = userService.signUp(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("masteruser");
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 username")
    void signUp_duplicateUsername() {
        // given
        SignUpRequest request = signUpRequest("masteruser", "Pass1!", "이름", "other@example.com");
        given(userRepository.existsByUsername("masteruser")).willReturn(true);

        // then
        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.DUPLICATE_USERNAME));

        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 email")
    void signUp_duplicateEmail() {
        // given
        SignUpRequest request = signUpRequest("uniqueuser", "Pass1!", "이름", "master@example.com");
        given(userRepository.existsByUsername("uniqueuser")).willReturn(false);
        given(userRepository.existsByEmail("master@example.com")).willReturn(true);

        // then
        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.DUPLICATE_EMAIL));
    }

    @Test
    @DisplayName("유저 단건 조회 성공")
    void getUser_success() {
        // given
        User user = TestFixtures.approvedMasterUser();
        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(user));

        // when
        UserResponse response = userService.getUser(TestFixtures.USER_ID);

        // then
        assertThat(response.getUserId()).isEqualTo(TestFixtures.USER_ID);
        assertThat(response.getUsername()).isEqualTo("masteruser");
    }

    @Test
    @DisplayName("유저 단건 조회 실패 - 존재하지 않는 유저")
    void getUser_notFound() {
        // given
        UUID unknownId = UUID.randomUUID();
        given(userRepository.findByUserIdAndDeletedAtIsNull(unknownId)).willReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> userService.getUser(unknownId))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("유저 정보 수정 성공")
    void updateUser_success() {
        // given
        User user = TestFixtures.approvedMasterUser();
        UpdateUserRequest request = updateUserRequest("변경이름", "changed@example.com", "U_NEW");
        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(user));

        // when
        UserResponse response = userService.updateUser(TestFixtures.USER_ID, request);

        // then
        assertThat(response.getName()).isEqualTo("변경이름");
        assertThat(response.getEmail()).isEqualTo("changed@example.com");
        then(deliveryManagerService).shouldHaveNoInteractions(); // MASTER 역할이므로 전파 없음
    }

    @Test
    @DisplayName("유저 정보 수정 - DELIVERY_MANAGER: slackId 변경 시 전파")
    void updateUser_deliveryManager_syncSlackId() {
        // given
        User user = TestFixtures.pendingDeliveryManagerUser();
        UpdateUserRequest request = updateUserRequest("배송매니저", "deliv@example.com", "U_NEW_SLACK");
        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(user));

        // when
        userService.updateUser(TestFixtures.USER_ID, request);

        // then
        then(deliveryManagerService).should().updateSlackId(TestFixtures.USER_ID, "U_NEW_SLACK");
    }

    @Test
    @DisplayName("유저 삭제 성공 - Redis 무효화 콜백 등록")
    void deleteUser_success() {
        // given
        User user = TestFixtures.approvedMasterUser();
        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(user));

        TransactionSynchronizationManager.initSynchronization();
        try {
            // when
            userService.deleteUser(TestFixtures.USER_ID, "admin");

            // then
            assertThat(user.getDeletedAt()).isNotNull();

            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit);
            then(redisUtil).should().invalidateUser(eq(TestFixtures.USER_ID.toString()), anyLong());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("유저 승인 - HUB_MANAGER: hubClient로 허브 존재 검증 후 executeApproval 호출")
    void approveUser_hubManager_success() {
        // given
        User user = TestFixtures.pendingHubManagerUser();
        ApproveUserRequest request = approveRequest(UserStatus.APPROVED, null);
        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(user));
        given(hubClient.checkHubExists(TestFixtures.HUB_ID)).willReturn(hubExistsResponse(true));

        // when
        userService.approveUser(TestFixtures.USER_ID, request, TestFixtures.ADMIN_ID);

        // then
        then(hubClient).should().checkHubExists(TestFixtures.HUB_ID);
        then(userApprovalService).should().executeApproval(TestFixtures.USER_ID, request, TestFixtures.ADMIN_ID);
    }

    @Test
    @DisplayName("유저 승인 - DELIVERY_MANAGER: deliveryManagerType 없으면 예외")
    void approveUser_deliveryManager_missingType() {
        // given
        User user = TestFixtures.pendingDeliveryManagerUser();
        ApproveUserRequest request = approveRequest(UserStatus.APPROVED, null); // type 없음
        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(user));

        // then
        assertThatThrownBy(() -> userService.approveUser(TestFixtures.USER_ID, request, TestFixtures.ADMIN_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.DELIVERY_TYPE_REQUIRED));
    }

    @Test
    @DisplayName("유저 승인 실패 - PENDING이 아닌 상태")
    void approveUser_notPendingStatus() {
        // given
        User user = TestFixtures.approvedMasterUser(); // 이미 APPROVED
        ApproveUserRequest request = approveRequest(UserStatus.APPROVED, null);
        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(user));

        // then
        assertThatThrownBy(() -> userService.approveUser(TestFixtures.USER_ID, request, TestFixtures.ADMIN_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.NOT_PENDING_STATUS));
    }

    private SignUpRequest signUpRequest(String username, String password, String name, String email) {
        SignUpRequest req = new SignUpRequest();
        ReflectionTestUtils.setField(req, "username", username);
        ReflectionTestUtils.setField(req, "password", password);
        ReflectionTestUtils.setField(req, "name", name);
        ReflectionTestUtils.setField(req, "email", email);
        ReflectionTestUtils.setField(req, "slackId", "U_TEST");
        ReflectionTestUtils.setField(req, "role", UserRole.MASTER);
        return req;
    }

    private UpdateUserRequest updateUserRequest(String name, String email, String slackId) {
        UpdateUserRequest req = new UpdateUserRequest();
        ReflectionTestUtils.setField(req, "name", name);
        ReflectionTestUtils.setField(req, "email", email);
        ReflectionTestUtils.setField(req, "slackId", slackId);
        return req;
    }

    private ApproveUserRequest approveRequest(UserStatus status, DeliveryManagerType type) {
        ApproveUserRequest req = new ApproveUserRequest();
        ReflectionTestUtils.setField(req, "status", status);
        ReflectionTestUtils.setField(req, "deliveryManagerType", type);
        return req;
    }

    private HubExistsResponse hubExistsResponse(boolean exists) {
        HubExistsResponse res = new HubExistsResponse();
        ReflectionTestUtils.setField(res, "exists", exists);
        return res;
    }
}
