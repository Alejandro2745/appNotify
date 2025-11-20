package com.sanch.appNotify.core;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.sanch.appNotify.config.RabbitConfig;
import com.sanch.appNotify.shared.events.DomainEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(String routingKey, DomainEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(routingKey, event);
                }
            });
        } else {
            send(routingKey, event);
        }
    }

    private void send(String routingKey, DomainEvent event) {
        log.debug("Publishing domain event {} with routing {}", event.getClass().getSimpleName(), routingKey);
        rabbitTemplate.convertAndSend(RabbitConfig.DOMAIN_EVENTS_EX, routingKey, event);
    }
}
