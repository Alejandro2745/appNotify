package com.sanch.appNotify.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sanch.appNotify.core.Dtos.*;
import com.sanch.appNotify.core.Dtos.AnnouncementRequest;
import com.sanch.appNotify.core.Dtos.DmRequest;
import com.sanch.appNotify.core.Dtos.PrefsRequest;
import com.sanch.appNotify.core.Dtos.TopicNotifyRequest;
import com.sanch.appNotify.core.MessagingService;
import com.sanch.appNotify.core.PreferenceService;
import com.sanch.appNotify.core.SseHub;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class Controllers {
    private final MessagingService bus;
    private final PreferenceService prefs;
    private final SseHub sse;

    // DM enviado
    @PostMapping("/dm")
    public void dm(@Valid @RequestBody DmRequest req) { bus.sendDm(req.from(), req.to(), req.text()); }

    // Cambiar preferencias de topics/temas
    @PostMapping("/prefs/{userId}")
    public String prefs(@PathVariable String userId, @Valid @RequestBody PrefsRequest req) {
        // Asegurarse de que la cola DM del usuario existe
        prefs.ensureDmQueue(userId);
        return prefs.setTopicPrefs(userId, req.topics());
    }

    // Notificar topic/tema
    @PostMapping("/notify")
    public void notifyTopic(@Valid @RequestBody TopicNotifyRequest req) { bus.publishTopic(req.topic(), req.payload()); }

    // Anuncio broadcast
    @PostMapping("/announcements")
    public void broadcast(@Valid @RequestBody AnnouncementRequest req) { bus.broadcast(req.text()); }

    // Stream SSE para un usuario
    @GetMapping(path = "/stream/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String userId) {
        prefs.ensureDmQueue(userId); // make sure exists
        return sse.register(userId);
    }
}