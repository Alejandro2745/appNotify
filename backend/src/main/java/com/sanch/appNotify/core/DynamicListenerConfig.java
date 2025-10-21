package com.sanch.appNotify.core;

import com.sanch.appNotify.core.SseHub;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

import static com.sanch.appNotify.config.RabbitConfig.*;

@Configuration
@RequiredArgsConstructor
public class DynamicListenerConfig {
    private final ConnectionFactory cf;
    private final AmqpAdmin admin;
    private final SseHub sse;
    private final RabbitListenerEndpointRegistry registry;

    private final ConcurrentHashMap<String, MessageListenerContainer> userDmContainers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MessageListenerContainer> userPrefContainers = new ConcurrentHashMap<>();

    public void ensureUserConsumers(String userId) {
        // DM queue
        String dmQ = "user."+userId+".queue";
        declare(dmQ);
        userDmContainers.computeIfAbsent(userId, id -> startContainer(dmQ, body -> sse.send(userId, Jsons.parse(body))));

        // Prefs queue
        String prefQ = "user."+userId+".prefs.queue";
        declare(prefQ);
        userPrefContainers.computeIfAbsent(userId, id -> startContainer(prefQ, body -> sse.send(userId, Jsons.parse(body))));
    }

    private void declare(String q) { admin.declareQueue(QueueBuilder.durable(q).build()); }

    private MessageListenerContainer startContainer(String queue, java.util.function.Consumer<String> onMsg) {
        SimpleMessageListenerContainer c = new SimpleMessageListenerContainer(cf);
        c.setQueueNames(queue);
        c.setMessageListener(message -> onMsg.accept(new String(message.getBody(), StandardCharsets.UTF_8)));
        c.start();
        return c;
    }

    // small helper
    static class Jsons {
        static String parse(byte[] body) { return new String(body, StandardCharsets.UTF_8); }
        static String parse(String body) { return body; }
    }
}