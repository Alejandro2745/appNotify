package com.sanch.appNotify.shared.events;

import java.time.Instant;

public record AnnouncementBroadcastedEvent(
        Long messageId,
        String text,
        Instant occurredAt
) implements DomainEvent { }
