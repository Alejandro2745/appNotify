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
@RequestMapping("/api")
@RequiredArgsConstructor
public class Controllers {
    private final MessagingService bus;
    private final PreferenceService prefs;
    private final SseHub sse;

    // DM send
    @PostMapping("/dm")
    public void dm(@Valid @RequestBody DmRequest req) { bus.sendDm(req.from(), req.to(), req.text()); }

    // Set/replace topic preferences for a user
    @PostMapping("/prefs/{userId}")
    public String prefs(@PathVariable String userId, @Valid @RequestBody PrefsRequest req) {
        // Ensure DM queue too (so stream can consume both)
        prefs.ensureDmQueue(userId);
        return prefs.setTopicPrefs(userId, req.topics());
    }

    // Publish a notification to a topic
    @PostMapping("/notify")
    public void notifyTopic(@Valid @RequestBody TopicNotifyRequest req) { bus.publishTopic(req.topic(), req.payload()); }

    // Broadcast announcement
    @PostMapping("/announcements")
    public void broadcast(@Valid @RequestBody AnnouncementRequest req) { bus.broadcast(req.text()); }

    // SSE stream per user: connect once from frontend
    @GetMapping(path = "/stream/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String userId) {
        prefs.ensureDmQueue(userId); // make sure exists
        return sse.register(userId);
    }
}