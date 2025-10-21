package com.sanch.appNotify.api;

import com.sanch.appNotify.core.DynamicListenerConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
public class StreamHook {
    private final DynamicListenerConfig dyn;

    // Called by controller after opening stream
    public void onStreamOpened(String userId, SseEmitter emitter) {
        dyn.ensureUserConsumers(userId);
    }
}