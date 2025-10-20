package com.sanch.appNotify.core;

import com.sanch.appNotify.core.SseHub;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Listeners {
    private final SseHub sse;

    // === DM: dynamic queues pattern ===
    @RabbitListener(queues = "all-users.queue") // announcements also go to SSE broadcast
    public void onAnnouncement(@Payload Map<String, Object> msg) {
        sse.broadcast(msg);
    }

    // Note: Per-user queues are dynamic (user.<id>.queue and user.<id>.prefs.queue). In Spring, we can
    // subscribe programmatically using listener container factory, but to keep it simple we piggyback
    // on RabbitMQ to route copies to a shared fanout per user registration (light approach below):
}