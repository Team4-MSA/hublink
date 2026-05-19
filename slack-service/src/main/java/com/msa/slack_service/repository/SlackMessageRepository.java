package com.msa.slack_service.repository;

import com.msa.slack_service.entity.SlackMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SlackMessageRepository extends JpaRepository<SlackMessage, UUID> {
}
