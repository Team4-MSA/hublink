package com.msa.slack_service.controller;

import com.msa.core_common.response.GlobalResponse;
import com.msa.core_common.response.paging.PageRes;
import com.msa.slack_service.dto.SlackMessageResponse;
import com.msa.slack_service.entity.MessageType;
import com.msa.slack_service.entity.SlackMessageStatus;
import com.msa.slack_service.service.SlackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/slack-messages")
public class SlackController {
    private final SlackService slackService;

    @GetMapping
    public PageRes<SlackMessageResponse> getSlackMessages(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) SlackMessageStatus status,
            @RequestParam(required = false) MessageType messageType,
            @PageableDefault(size = 10, sort = "sentAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return slackService.getSlackMessages(role, status, messageType, pageable);
    }

    @GetMapping("/{slackMessageId}")
    public SlackMessageResponse getSlackMessage(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID slackMessageId
    ) {
        return slackService.getSlackMessage(role, slackMessageId);
    }

    @PostMapping("/{slackMessageId}/resend")
    public GlobalResponse<Void> resendSlackMessage(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID slackMessageId
    ) {
        slackService.resendSlackMessage(role, slackMessageId);
        return GlobalResponse.success(200, null);
    }
}
