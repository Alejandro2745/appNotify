package com.sanch.appNotify.core;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Listeners {
    private final SseHub sse;

    // === DM: dinamicos ===
    @RabbitListener(queues = "all-users.queue") // cola de anuncios broadcast
    public void onAnnouncement(@Payload Map<String, Object> msg) {
        sse.broadcast(msg);
    }


}