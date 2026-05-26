package com.msa.user_service.repository;

import com.msa.user_service.entity.UserApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserApprovalHistoryRepository extends JpaRepository<UserApprovalHistory, UUID> {
    List<UserApprovalHistory> findByUserIdOrderByProcessedAtDesc(UUID userId);
}
