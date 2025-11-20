package com.sanch.appNotify.shared.events;

import java.time.Instant;
import java.util.Set;

public record PreferenceUpdatedEvent(
        String userId,
        Set<String> topics,
        Instant occurredAt
) implements DomainEvent { }
