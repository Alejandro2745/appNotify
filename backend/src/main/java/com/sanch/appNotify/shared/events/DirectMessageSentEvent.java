package com.sanch.appNotify.shared.events;

import java.time.Instant;

public record DirectMessageSentEvent(
        Long messageId,
        String fromUser,
        String toUser,
        String text,
        Instant occurredAt
) implements DomainEvent { }
