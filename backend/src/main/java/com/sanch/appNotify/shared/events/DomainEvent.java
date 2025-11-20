package com.sanch.appNotify.shared.events;

import java.time.Instant;

/**
 * Marker interface for domain events that travel across the messaging bus to
 * keep the query side eventually consistent.
 */
public interface DomainEvent {
    Instant occurredAt();
}
