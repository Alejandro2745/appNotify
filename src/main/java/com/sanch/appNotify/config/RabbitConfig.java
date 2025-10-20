package com.sanch.appNotify.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String DM_EX = "dm.exchange";            // direct
    public static final String NOTIFY_EX = "notify.exchange";    // topic
    public static final String ANN_EX = "announcements.exchange";// fanout

    @Bean DirectExchange dmExchange() { return new DirectExchange(DM_EX, true, false); }
    @Bean TopicExchange notifyExchange() { return new TopicExchange(NOTIFY_EX, true, false); }
    @Bean FanoutExchange annExchange() { return new FanoutExchange(ANN_EX, true, false); }

    @Bean Queue allUsersQ() { return QueueBuilder.durable("all-users.queue").build(); }
    @Bean Queue auditAnnQ() { return QueueBuilder.durable("audit.ann.queue").build(); }
    @Bean Binding allUsersB(Queue allUsersQ, FanoutExchange annExchange) {
        return BindingBuilder.bind(allUsersQ).to(annExchange);
    }
    @Bean Binding auditAnnB(Queue auditAnnQ, FanoutExchange annExchange) {
        return BindingBuilder.bind(auditAnnQ).to(annExchange);
    }
}
