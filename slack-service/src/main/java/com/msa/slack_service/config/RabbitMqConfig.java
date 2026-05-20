package com.msa.slack_service.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String HUBLINK_EXCHANGE = "hublink.exchange";
    public static final String SLACK_DEADLINE_GENERATED_QUEUE = "slack.deadline.generated.queue";
    public static final String DEADLINE_GENERATED_ROUTING_KEY = "deadline.generated";

    @Bean
    public TopicExchange hublinkExchange() {
        return new TopicExchange(HUBLINK_EXCHANGE);
    }

    @Bean
    public Queue slackDeadlineGeneratedQueue() {
        return new Queue(SLACK_DEADLINE_GENERATED_QUEUE, true);
    }

    @Bean
    public Binding slackDeadlineGeneratedBinding() {
        return BindingBuilder
                .bind(slackDeadlineGeneratedQueue())
                .to(hublinkExchange())
                .with(DEADLINE_GENERATED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
