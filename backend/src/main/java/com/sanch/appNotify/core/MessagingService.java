package com.sanch.appNotify.core;

import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.sanch.appNotify.config.RabbitConfig.ANN_EX;
import static com.sanch.appNotify.config.RabbitConfig.DM_EX;
import static com.sanch.appNotify.config.RabbitConfig.NOTIFY_EX;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessagingService {
    private final RabbitTemplate rabbit;

    // direct: DM para un usuario en cola user.<id>.queue
    public void sendDm(String from, String to, String text) {
        String routingKey = "user." + to;
        var msg = Map.of("type","dm","from",from,"to",to,"text",text,"ts",System.currentTimeMillis());
        rabbit.convertAndSend(DM_EX, routingKey, msg);
    }

    // topic: Publico al que le interesa (e.g., notify.tech.ai)
    public void publishTopic(String topic, String payload) {
        String rk = topic.startsWith("notify.") ? topic : ("notify." + topic);
        var msg = Map.of("type","topic","topic",rk,"payload",payload,"ts",System.currentTimeMillis());
        rabbit.convertAndSend(NOTIFY_EX, rk, msg);
    }

    // fanout: Anuncio broadcast a todos
    public void broadcast(String text) {
        var msg = Map.of("type","announcement","text",text,"ts",System.currentTimeMillis());
        rabbit.convertAndSend(ANN_EX, "", msg);
    }
}