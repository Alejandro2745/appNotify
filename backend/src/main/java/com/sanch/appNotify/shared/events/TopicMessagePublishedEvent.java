package com.sanch.appNotify.shared.events;

import java.time.Instant;

public record TopicMessagePublishedEvent(
        Long messageId,
        String topic,
        String payload,
        Instant occurredAt
) implements DomainEvent { }
