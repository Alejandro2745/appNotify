package com.sanch.appNotify.core;

import java.time.Instant;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.sanch.appNotify.config.RabbitConfig.ANN_EX;
import static com.sanch.appNotify.config.RabbitConfig.DM_EX;
import static com.sanch.appNotify.config.RabbitConfig.NOTIFY_EX;
import com.sanch.appNotify.command.MessageRecordEntity;
import com.sanch.appNotify.command.MessageRecordRepository;
import com.sanch.appNotify.command.MessageRecordType;
import com.sanch.appNotify.shared.events.AnnouncementBroadcastedEvent;
import com.sanch.appNotify.shared.events.DirectMessageSentEvent;
import com.sanch.appNotify.shared.events.TopicMessagePublishedEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final RabbitTemplate rabbit;
    private final MessageRecordRepository messageRecordRepository;
    private final DomainEventPublisher eventPublisher;

    // direct: DM para un usuario en cola user.<id>.queue
    public void sendDm(String from, String to, String text) {
        String routingKey = "user." + to;
        Instant now = Instant.now();

        MessageRecordEntity record = messageRecordRepository.save(MessageRecordEntity.builder()
                .type(MessageRecordType.DM)
                .fromUser(from)
                .toUser(to)
                .text(text)
                .createdAt(now)
                .build());

        var msg = Map.of(
                "type", "dm",
                "from", from,
                "to", to,
                "text", text,
                "ts", now.toEpochMilli()
        );
        rabbit.convertAndSend(DM_EX, routingKey, msg);

        eventPublisher.publish("dm.sent", new DirectMessageSentEvent(
                record.getId(),
                from,
                to,
                text,
                record.getCreatedAt()
        ));
    }

    // topic: Público al que le interesa (e.g., notify.tech.ai)
    public void publishTopic(String topic, String payload) {
        String rk = topic.startsWith("notify.") ? topic : ("notify." + topic);
        Instant now = Instant.now();

        MessageRecordEntity record = messageRecordRepository.save(MessageRecordEntity.builder()
                .type(MessageRecordType.TOPIC)
                .topic(rk)
                .payload(payload)
                .createdAt(now)
                .build());

        var msg = Map.of(
                "type", "topic",
                "topic", rk,
                "payload", payload,
                "ts", now.toEpochMilli()
        );
        rabbit.convertAndSend(NOTIFY_EX, rk, msg);

        eventPublisher.publish("topic.published", new TopicMessagePublishedEvent(
                record.getId(),
                rk,
                payload,
                record.getCreatedAt()
        ));
    }

    // fanout: Anuncio broadcast a todos
    public void broadcast(String text) {
        Instant now = Instant.now();

        MessageRecordEntity record = messageRecordRepository.save(MessageRecordEntity.builder()
                .type(MessageRecordType.ANNOUNCEMENT)
                .text(text)
                .createdAt(now)
                .build());

        var msg = Map.of(
                "type", "announcement",
                "text", text,
                "ts", now.toEpochMilli()
        );
        rabbit.convertAndSend(ANN_EX, "", msg);

        eventPublisher.publish("announcement.broadcasted", new AnnouncementBroadcastedEvent(
                record.getId(),
                text,
                record.getCreatedAt()
        ));
    }
}
