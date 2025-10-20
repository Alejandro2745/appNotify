package com.sanch.appNotify.api;

import com.sanch.appNotify.core.Dtos.*;
import com.sanch.appNotify.core.MessagingService;
import com.sanch.appNotify.core.PreferenceService;
import com.sanch.appNotify.core.SseHub;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api2")
@RequiredArgsConstructor
public class ControllersUpdate {
    private final MessagingService bus;
    private final PreferenceService prefs;
    private final SseHub sse;
    private final StreamHook hook;

    @GetMapping(path = "/stream/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String userId) {
        prefs.ensureDmQueue(userId);
        SseEmitter em = sse.register(userId);
        hook.onStreamOpened(userId, em);
        return em;
    }
}