package com.sanch.appNotify.core;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import static com.sanch.appNotify.config.RabbitConfig.*;

@Service
@RequiredArgsConstructor
public class MessagingService {
    private final RabbitTemplate rabbit;

    // direct: DM to a single user queue user.<id>.queue
    public void sendDm(String from, String to, String text) {
        String routingKey = "user." + to;
        var msg = Map.of("type","dm","from",from,"to",to,"text",text,"ts",System.currentTimeMillis());
        rabbit.convertAndSend(DM_EX, routingKey, msg);
    }

    // topic: publish to interest (e.g., notify.tech.ai)
    public void publishTopic(String topic, String payload) {
        String rk = topic.startsWith("notify.") ? topic : ("notify." + topic);
        var msg = Map.of("type","topic","topic",rk,"payload",payload,"ts",System.currentTimeMillis());
        rabbit.convertAndSend(NOTIFY_EX, rk, msg);
    }

    // fanout: announcement to all
    public void broadcast(String text) {
        var msg = Map.of("type","announcement","text",text,"ts",System.currentTimeMillis());
        rabbit.convertAndSend(ANN_EX, "", msg);
    }
}