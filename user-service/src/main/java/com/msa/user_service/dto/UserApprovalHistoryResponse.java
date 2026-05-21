package com.msa.user_service.dto;

import com.msa.user_service.entity.UserApprovalHistory;
import com.msa.user_service.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserApprovalHistoryResponse {
    private UUID approvalHistoryId;
    private UUID userId;
    private UserStatus previousStatus;
    private UserStatus newStatus;
    private String reason;
    private UUID processedBy;
    private LocalDateTime processedAt;

    public static UserApprovalHistoryResponse from(UserApprovalHistory history) {
        return UserApprovalHistoryResponse.builder()
                .approvalHistoryId(history.getApprovalHistoryId())
                .userId(history.getUserId())
                .previousStatus(history.getPreviousStatus())
                .newStatus(history.getNewStatus())
                .reason(history.getReason())
                .processedBy(history.getProcessedBy())
                .processedAt(history.getProcessedAt())
                .build();
    }
}
