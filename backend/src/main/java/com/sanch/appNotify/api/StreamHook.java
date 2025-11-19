package com.sanch.appNotify.api;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sanch.appNotify.core.DynamicListenerConfig;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StreamHook {
    private final DynamicListenerConfig dyn;

    // Cuando se abre un stream SSE para un usuario, asegurar consumidores dinámicos
    public void onStreamOpened(String userId, SseEmitter emitter) {
        dyn.ensureUserConsumers(userId);
    }
}