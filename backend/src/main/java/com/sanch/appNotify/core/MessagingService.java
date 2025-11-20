package com.sanch.appNotify.core;

import java.time.Instant;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.sanch.appNotify.config.RabbitConfig.ANN_EX;
import static com.sanch.appNotify.config.RabbitConfig.DM_EX;
import static com.sanch.appNotify.config.RabbitConfig.NOTIFY_EX;
import com.sanch.appNotify.query.MessageDocument;
import com.sanch.appNotify.query.MessageHistoryRepository;
import com.sanch.appNotify.query.MessageType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final RabbitTemplate rabbit;
    private final MessageHistoryRepository history;  

    // direct: DM para un usuario en cola user.<id>.queue
    public void sendDm(String from, String to, String text) {
        String routingKey = "user." + to;
        Instant now = Instant.now();

        history.save(MessageDocument.builder()
                .type(MessageType.DM)
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
    }

    // topic: Público al que le interesa (e.g., notify.tech.ai)
    public void publishTopic(String topic, String payload) {
        String rk = topic.startsWith("notify.") ? topic : ("notify." + topic);
        Instant now = Instant.now();

        history.save(MessageDocument.builder()
                .type(MessageType.TOPIC)
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
    }

    // fanout: Anuncio broadcast a todos
    public void broadcast(String text) {
        Instant now = Instant.now();

        history.save(MessageDocument.builder()
                .type(MessageType.ANNOUNCEMENT)
                .text(text)
                .createdAt(now)
                .build());

        var msg = Map.of(
                "type", "announcement",
                "text", text,
                "ts", now.toEpochMilli()
        );
        rabbit.convertAndSend(ANN_EX, "", msg);
    }
}
