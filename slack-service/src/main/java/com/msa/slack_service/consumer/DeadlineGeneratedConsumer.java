package com.msa.slack_service.consumer;

import com.msa.slack_service.config.RabbitMqConfig;
import com.msa.slack_service.dto.DeadlineGeneratedEvent;
import com.msa.slack_service.service.SlackService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeadlineGeneratedConsumer {
    private final SlackService slackService;

    @RabbitListener(queues = RabbitMqConfig.SLACK_DEADLINE_GENERATED_QUEUE)
    public void consume(DeadlineGeneratedEvent event) {
        slackService.processDeadlineGenerated(event);
    }
}
