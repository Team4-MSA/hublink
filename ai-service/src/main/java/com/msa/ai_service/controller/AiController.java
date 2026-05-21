package com.msa.ai_service.controller;

import com.msa.ai_service.dto.AiMessageResponse;
import com.msa.ai_service.entity.AiMessageStatus;
import com.msa.ai_service.entity.AiRequestType;
import com.msa.ai_service.service.AiMessageService;
import com.msa.core_common.response.paging.PageRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai-messages")
public class AiController {
    private final AiMessageService aiMessageService;

    // AI 생성 이력 목록 조회
    @GetMapping
    public PageRes<AiMessageResponse> getAiMessages(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(name = "request_type", required = false) AiRequestType requestType,
            @RequestParam(required = false) AiMessageStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return aiMessageService.getAiMessages(role, requestType, status, pageable);
    }

    // AI 생성 이력 상세 조회
    @GetMapping("/{aiMessageId}")
    public AiMessageResponse getAiMessage(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID aiMessageId
    ) {
        return aiMessageService.getAiMessage(role, aiMessageId);
    }
}