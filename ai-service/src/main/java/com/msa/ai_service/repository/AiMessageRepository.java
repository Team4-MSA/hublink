package com.msa.ai_service.repository;

import com.msa.ai_service.entity.AiMessage;
import com.msa.ai_service.entity.AiMessageStatus;
import com.msa.ai_service.entity.AiRequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AiMessageRepository extends JpaRepository<AiMessage, UUID> {
    Optional<AiMessage> findByAiMessageIdAndDeletedAtIsNull(UUID aiMessageId);

    @Query("""
        SELECT a
        FROM AiMessage a
        WHERE a.deletedAt IS NULL
          AND (:requestType IS NULL OR a.requestType = :requestType)
          AND (:status IS NULL OR a.status = :status)
        """)
    Page<AiMessage> findAllByCondition(
            @Param("requestType") AiRequestType requestType,
            @Param("status") AiMessageStatus status,
            Pageable pageable
    );
}
