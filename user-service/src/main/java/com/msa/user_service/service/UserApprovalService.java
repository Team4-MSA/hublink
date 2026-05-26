package com.msa.user_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.user_service.dto.ApproveUserRequest;
import com.msa.user_service.entity.User;
import com.msa.user_service.entity.UserApprovalHistory;
import com.msa.user_service.entity.UserRole;
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
    private final DeliveryManagerService deliveryManagerService;

    @Transactional
    public void executeApproval(UUID userId, ApproveUserRequest request, UUID processedBy) {

        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new CustomException(UserErrorCode.NOT_PENDING_STATUS);
        }

        UserStatus previousStatus = user.getStatus();

        if (request.getStatus() == UserStatus.APPROVED) {
            user.approve();

            if (user.getRole() == UserRole.DELIVERY_MANAGER) {
                deliveryManagerService.createOnApproval(user.getUserId(), user.getHubId(),
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
