package com.msa.slack_service.controller;

import com.msa.slack_service.dto.SlackMessageResponse;
import com.msa.slack_service.service.SlackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/slack-messages")
public class SlackController {
    private final SlackService slackService;

    @GetMapping
    public List<SlackMessageResponse> getSlackMessages(
            @RequestHeader("X-User-Role") String role
    ) {
        return slackService.getSlackMessages(role);
    }

    @GetMapping("/{slackMessageId}")
    public SlackMessageResponse getSlackMessage(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID slackMessageId
    ) {
        return slackService.getSlackMessage(role, slackMessageId);
    }

    @PostMapping("/{slackMessageId}/resend")
    public String resendSlackMessage(
            @RequestHeader("X-User-Role") String role,
            @PathVariable UUID slackMessageId
    ) {
        slackService.resendSlackMessage(role, slackMessageId);
        return "Slack message resent";
    }
}
