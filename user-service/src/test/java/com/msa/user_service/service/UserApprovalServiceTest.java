package com.msa.user_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.user_service.dto.ApproveUserRequest;
import com.msa.user_service.entity.*;
import com.msa.user_service.fixture.TestFixtures;
import com.msa.user_service.global.UserErrorCode;
import com.msa.user_service.repository.UserApprovalHistoryRepository;
import com.msa.user_service.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserApprovalService 테스트")
class UserApprovalServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserApprovalHistoryRepository approvalHistoryRepository;
    @Mock private HubManagerService hubManagerService;
    @Mock private CompanyManagerService companyManagerService;
    @Mock private DeliveryManagerService deliveryManagerService;

    @InjectMocks
    private UserApprovalService userApprovalService;

    @Test
    @DisplayName("HUB_MANAGER 승인 - hubManagerService.createOnApproval 호출")
    void executeApproval_approve_hubManager() {
        // given
        User user = TestFixtures.pendingHubManagerUser();
        ApproveUserRequest request = approveRequest(UserStatus.APPROVED, null);

        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(user));
        given(approvalHistoryRepository.save(any(UserApprovalHistory.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        userApprovalService.executeApproval(TestFixtures.USER_ID, request, TestFixtures.ADMIN_ID);

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.APPROVED);
        then(hubManagerService).should().createOnApproval(TestFixtures.USER_ID, TestFixtures.HUB_ID);
        then(approvalHistoryRepository).should().save(any(UserApprovalHistory.class));
    }

    @Test
    @DisplayName("COMPANY_MANAGER 승인 - companyManagerService.createOnApproval 호출")
    void executeApproval_approve_companyManager() {
        // given
        User user = TestFixtures.pendingCompanyManagerUser();
        ApproveUserRequest request = approveRequest(UserStatus.APPROVED, null);

        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(user));
        given(approvalHistoryRepository.save(any(UserApprovalHistory.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        userApprovalService.executeApproval(TestFixtures.USER_ID, request, TestFixtures.ADMIN_ID);

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.APPROVED);
        then(companyManagerService).should().createOnApproval(TestFixtures.USER_ID, TestFixtures.COMPANY_ID);
    }

    @Test
    @DisplayName("DELIVERY_MANAGER 승인 - deliveryManagerService.createOnApproval 호출")
    void executeApproval_approve_deliveryManager() {
        // given
        User user = TestFixtures.pendingDeliveryManagerUser();
        ApproveUserRequest request = approveRequest(UserStatus.APPROVED, DeliveryManagerType.HUB_DELIVERY);

        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(user));
        given(approvalHistoryRepository.save(any(UserApprovalHistory.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        userApprovalService.executeApproval(TestFixtures.USER_ID, request, TestFixtures.ADMIN_ID);

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.APPROVED);
        then(deliveryManagerService).should().createOnApproval(
                TestFixtures.USER_ID, TestFixtures.HUB_ID,
                DeliveryManagerType.HUB_DELIVERY, user.getSlackId());
    }

    @Test
    @DisplayName("거절 처리 - 상태가 REJECTED로 변경")
    void executeApproval_reject() {
        // given
        User user = TestFixtures.pendingHubManagerUser();
        ApproveUserRequest request = approveRequest(UserStatus.REJECTED, null);

        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(user));
        given(approvalHistoryRepository.save(any(UserApprovalHistory.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        userApprovalService.executeApproval(TestFixtures.USER_ID, request, TestFixtures.ADMIN_ID);

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.REJECTED);
        then(hubManagerService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("비활성화 처리 - 상태가 INACTIVE로 변경")
    void executeApproval_inactive() {
        // given
        User user = TestFixtures.pendingHubManagerUser();
        ApproveUserRequest request = approveRequest(UserStatus.INACTIVE, null);

        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(user));
        given(approvalHistoryRepository.save(any(UserApprovalHistory.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        userApprovalService.executeApproval(TestFixtures.USER_ID, request, TestFixtures.ADMIN_ID);

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    @DisplayName("승인 실패 - PENDING이 아닌 상태")
    void executeApproval_notPendingStatus() {
        // given
        User user = TestFixtures.approvedMasterUser(); // 이미 APPROVED
        ApproveUserRequest request = approveRequest(UserStatus.APPROVED, null);

        given(userRepository.findByUserIdAndDeletedAtIsNull(TestFixtures.USER_ID))
                .willReturn(Optional.of(user));

        // then
        assertThatThrownBy(() ->
                userApprovalService.executeApproval(TestFixtures.USER_ID, request, TestFixtures.ADMIN_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(UserErrorCode.NOT_PENDING_STATUS));
    }

    private ApproveUserRequest approveRequest(UserStatus status, DeliveryManagerType type) {
        ApproveUserRequest req = new ApproveUserRequest();
        ReflectionTestUtils.setField(req, "status", status);
        ReflectionTestUtils.setField(req, "deliveryManagerType", type);
        return req;
    }
}
