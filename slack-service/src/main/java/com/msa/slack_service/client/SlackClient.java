package com.msa.slack_service.client;

import com.msa.core_common.error.exception.CustomException;
import com.msa.slack_service.dto.SlackApiRequest;
import com.msa.slack_service.dto.SlackApiResponse;
import com.msa.slack_service.exception.SlackErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class SlackClient {
    private final RestClient restClient;

    @Value("${slack.bot-token}")
    private String botToken;

    public void sendMessage(String receiverSlackId, String message) {
        SlackApiResponse response = restClient.post()
                .uri("/api/chat.postMessage")
                .header("Authorization", "Bearer " + botToken)
                .header("Content-Type", "application/json; charset=utf-8")
                .body(new SlackApiRequest(receiverSlackId, message))
                .retrieve()
                .body(SlackApiResponse.class);

        if (response == null || !response.isOk()) {
            throw new CustomException(SlackErrorCode.SLACK_MESSAGE_SEND_FAILED);
        }
    }
}