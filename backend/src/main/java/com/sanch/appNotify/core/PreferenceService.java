package com.sanch.appNotify.core;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.stereotype.Service;

import com.sanch.appNotify.config.RabbitConfig;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import static com.sanch.appNotify.config.RabbitConfig.*;

@Service
@RequiredArgsConstructor
public class PreferenceService {
    private final AmqpAdmin amqpAdmin;
    private final Map<String, Set<String>> userTopics = new ConcurrentHashMap<>();

    // Ensure DM queue exists for a user
    public String ensureDmQueue(String userId) {
        String q = "user."+userId+".queue";
        Queue queue = QueueBuilder.durable(q).build();
        amqpAdmin.declareQueue(queue);
        Binding b = BindingBuilder.bind(queue).to(new DirectExchange(DM_EX)).with("user."+userId);
        amqpAdmin.declareBinding(b);
        return q;
    }

public String setTopicPrefs(String userId, List<String> topics) {
    // Normaliza routing keys al prefijo notify.
    Set<String> desired = new HashSet<>();
    for (String t : topics) {
        if (t == null) continue;
        String rk = t.trim();
        if (rk.isEmpty()) continue;
        if (!rk.startsWith("notify.")) rk = "notify." + rk;
        desired.add(rk);
    }

    String q = "user." + userId + ".prefs.queue";
    Queue queue = QueueBuilder.durable(q).build();
    amqpAdmin.declareQueue(queue);

    TopicExchange ex = new TopicExchange(RabbitConfig.NOTIFY_EX, true, false);
    amqpAdmin.declareExchange(ex);

    Set<String> previous = userTopics.getOrDefault(userId, Set.of());

    Set<String> toRemove = new HashSet<>(previous);
    toRemove.removeAll(desired);

    Set<String> toAdd = new HashSet<>(desired);
    toAdd.removeAll(previous);

    
    for (String rk : toRemove) {
        Binding b = BindingBuilder.bind(new Queue(q)).to(ex).with(rk);
        amqpAdmin.removeBinding(b);
    }

    for (String rk : toAdd) {
        Binding b = BindingBuilder.bind(queue).to(ex).with(rk);
        amqpAdmin.declareBinding(b);
    }

    userTopics.put(userId, desired);
    amqpAdmin.purgeQueue(q, false);

    return q;
    }

}