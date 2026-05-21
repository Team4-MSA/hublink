package com.msa.slack_service.repository;

import com.msa.slack_service.entity.MessageType;
import com.msa.slack_service.entity.SlackMessage;
import com.msa.slack_service.entity.SlackMessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SlackMessageRepository extends JpaRepository<SlackMessage, UUID> {
    Optional<SlackMessage> findByIdempotencyKey(String idempotencyKey);
    @Query("""
    SELECT sm
    FROM SlackMessage sm
    WHERE (:status IS NULL OR sm.status = :status)
      AND (:messageType IS NULL OR sm.messageType = :messageType)
""")
    Page<SlackMessage> findAllByCondition(
            @Param("status") SlackMessageStatus status,
            @Param("messageType") MessageType messageType,
            Pageable pageable
    );
}
