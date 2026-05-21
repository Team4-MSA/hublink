package com.msa.ai_service.repository;

import com.msa.ai_service.entity.AiMessage;
import com.msa.ai_service.entity.AiRequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiMessageRepository extends JpaRepository<AiMessage, UUID> {
    Page<AiMessage> findAllByDeletedAtIsNull(Pageable pageable);

    Page<AiMessage> findAllByRequestTypeAndDeletedAtIsNull(
            AiRequestType requestType,
            Pageable pageable
    );

    Optional<AiMessage> findByAiMessageIdAndDeletedAtIsNull(UUID aiMessageId);
}
