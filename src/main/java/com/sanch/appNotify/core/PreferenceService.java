package com.sanch.appNotify.core;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.stereotype.Service;
import java.util.List;

import static com.sanch.appNotify.config.RabbitConfig.*;

@Service
@RequiredArgsConstructor
public class PreferenceService {
    private final AmqpAdmin amqpAdmin;

    // Ensure DM queue exists for a user
    public String ensureDmQueue(String userId) {
        String q = "user."+userId+".queue";
        Queue queue = QueueBuilder.durable(q).build();
        amqpAdmin.declareQueue(queue);
        Binding b = BindingBuilder.bind(queue).to(new DirectExchange(DM_EX)).with("user."+userId);
        amqpAdmin.declareBinding(b);
        return q;
    }

    // Ensure topic prefs queue exists and bindings match provided topics
    public String setTopicPrefs(String userId, List<String> topics) {
        String q = "user."+userId+".prefs.queue";
        Queue queue = QueueBuilder.durable(q).build();
        amqpAdmin.declareQueue(queue);
        TopicExchange ex = new TopicExchange(NOTIFY_EX);
        // First, unbind all existing bindings (best-effort: not tracked here). In a real app, persist current.
        // Bind new topics
        for (String t : topics) {
            String rk = t.startsWith("notify.") ? t : ("notify."+t);
            Binding b = BindingBuilder.bind(queue).to(ex).with(rk);
            amqpAdmin.declareBinding(b);
        }
        return q;
    }
}