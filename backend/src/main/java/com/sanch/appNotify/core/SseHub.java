package com.sanch.appNotify.core;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseHub {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter register(String userId) {
        SseEmitter em = new SseEmitter(0L); // sin timeout
        emitters.put(userId, em);
        em.onCompletion(() -> emitters.remove(userId));
        em.onTimeout(() -> emitters.remove(userId));
        return em;
    }

    public void send(String userId, Object data) {
        SseEmitter em = emitters.get(userId);
        if (em != null) {
            try { em.send(SseEmitter.event().name("message").data(data)); }
            catch (IOException e) { emitters.remove(userId); }
        }
    }

    public void broadcast(Object data) {
        emitters.forEach((id, em) -> {
            try { em.send(SseEmitter.event().name("message").data(data)); }
            catch (IOException e) { emitters.remove(id); }
        });
    }
}