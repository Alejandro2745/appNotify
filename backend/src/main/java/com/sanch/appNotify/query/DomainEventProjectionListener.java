package com.sanch.appNotify.query;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.sanch.appNotify.config.RabbitConfig;
import com.sanch.appNotify.shared.events.AnnouncementBroadcastedEvent;
import com.sanch.appNotify.shared.events.DirectMessageSentEvent;
import com.sanch.appNotify.shared.events.PreferenceUpdatedEvent;
import com.sanch.appNotify.shared.events.TopicMessagePublishedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RabbitListener(queues = RabbitConfig.DOMAIN_EVENTS_QUEUE)
@RequiredArgsConstructor
@Slf4j
public class DomainEventProjectionListener {

    private final MessageHistoryRepository historyRepository;

    @RabbitHandler
    public void handle(DirectMessageSentEvent event) {
        historyRepository.save(MessageDocument.builder()
                .type(MessageType.DM)
                .fromUser(event.fromUser())
                .toUser(event.toUser())
                .text(event.text())
                .createdAt(event.occurredAt())
                .build());
    }

    @RabbitHandler
    public void handle(TopicMessagePublishedEvent event) {
        historyRepository.save(MessageDocument.builder()
                .type(MessageType.TOPIC)
                .topic(event.topic())
                .payload(event.payload())
                .createdAt(event.occurredAt())
                .build());
    }

    @RabbitHandler
    public void handle(AnnouncementBroadcastedEvent event) {
        historyRepository.save(MessageDocument.builder()
                .type(MessageType.ANNOUNCEMENT)
                .text(event.text())
                .createdAt(event.occurredAt())
                .build());
    }

    @RabbitHandler
    public void handle(PreferenceUpdatedEvent event) {
        log.info("PreferenceUpdatedEvent received for user {} topics {}", event.userId(), event.topics());
    }
}
