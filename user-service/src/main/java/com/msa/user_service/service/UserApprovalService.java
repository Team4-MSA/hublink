package com.msa.user_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.user_service.dto.ApproveUserRequest;
import com.msa.user_service.entity.User;
import com.msa.user_service.entity.UserApprovalHistory;
import com.msa.user_service.entity.UserStatus;
import com.msa.user_service.global.UserErrorCode;
import com.msa.user_service.repository.UserApprovalHistoryRepository;
import com.msa.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserApprovalService {

    private final UserRepository userRepository;
    private final UserApprovalHistoryRepository approvalHistoryRepository;
    private final HubManagerService hubManagerService;
    private final CompanyManagerService companyManagerService;
    private final DeliveryManagerService deliveryManagerService;

    @Transactional
    public void executeApproval(UUID userId, ApproveUserRequest request, UUID processedBy) {
        // 트랜잭션 내에서 User를 직접 조회해야 영속성 컨텍스트에서 관리됨
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        UserStatus previousStatus = user.getStatus();

        if (request.getStatus() == UserStatus.APPROVED) {
            user.approve();

            switch (user.getRole()) {
                case HUB_MANAGER -> hubManagerService.createOnApproval(user.getUserId(), user.getHubId());
                case COMPANY_MANAGER -> companyManagerService.createOnApproval(user.getUserId(), user.getCompanyId());
                case DELIVERY_MANAGER -> deliveryManagerService.createOnApproval(user.getUserId(), user.getHubId(),
                        request.getDeliveryManagerType(), user.getSlackId());
            }
        } else if (request.getStatus() == UserStatus.REJECTED) {
            user.reject();
        } else if (request.getStatus() == UserStatus.INACTIVE) {
            user.inactive();
        }

        approvalHistoryRepository.save(UserApprovalHistory.builder()
                .userId(user.getUserId())
                .previousStatus(previousStatus)
                .newStatus(request.getStatus())
                .reason(request.getReason())
                .processedBy(processedBy)
                .build());
    }
}
