package com.sanch.appNotify.core;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanch.appNotify.command.TopicPreferenceEntity;
import com.sanch.appNotify.command.TopicPreferenceRepository;
import com.sanch.appNotify.command.UserEntity;
import com.sanch.appNotify.command.UserRepository;
import com.sanch.appNotify.config.RabbitConfig;
import static com.sanch.appNotify.config.RabbitConfig.DM_EX;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreferenceService {
    private final AmqpAdmin amqpAdmin;
    private final UserRepository userRepository;
    private final TopicPreferenceRepository topicPreferenceRepository;
    private final Map<String, Set<String>> userTopics = new ConcurrentHashMap<>();

    @Transactional
    public void ensureUserRegistered(String userId) {
        userRepository.findById(userId)
                .orElseGet(() -> userRepository.save(UserEntity.builder().userId(userId).build()));
    }

    // Ensure DM queue exists for a user
    @Transactional
    public String ensureDmQueue(String userId) {
        ensureUserRegistered(userId);
        String q = "user."+userId+".queue";
        Queue queue = QueueBuilder.durable(q).build();
        amqpAdmin.declareQueue(queue);
        Binding b = BindingBuilder.bind(queue).to(new DirectExchange(DM_EX)).with("user."+userId);
        amqpAdmin.declareBinding(b);
        return q;
    }

    @Transactional
    public String setTopicPrefs(String userId, List<String> topics) {
        ensureUserRegistered(userId);
        Set<String> desired = normalizeTopics(topics);
        persistPreferences(userId, desired);
        return applyTopicBindings(userId, desired);
    }

    @Transactional(readOnly = true)
    public Map<String, Set<String>> fetchAllPreferences() {
        Map<String, Set<String>> snapshot = new LinkedHashMap<>();
        userRepository.findAll().forEach(user -> {
            List<TopicPreferenceEntity> prefs = topicPreferenceRepository.findByUser_UserId(user.getUserId());
            Set<String> topics = prefs.stream()
                    .map(TopicPreferenceEntity::getTopic)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            snapshot.put(user.getUserId(), topics);
        });
        return snapshot;
    }

    public void reapplyBindings(String userId, Set<String> topics) {
        applyTopicBindings(userId, topics);
    }

    private Set<String> normalizeTopics(List<String> topics) {
        Set<String> desired = new LinkedHashSet<>();
        if (topics == null) {
            return desired;
        }
        for (String t : topics) {
            if (t == null) continue;
            String rk = t.trim();
            if (rk.isEmpty()) continue;
            if (!rk.startsWith("notify.")) rk = "notify." + rk;
            desired.add(rk);
        }
        return desired;
    }

    private void persistPreferences(String userId, Set<String> desired) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado tras ensure"));
        topicPreferenceRepository.deleteByUser_UserId(userId);
        if (!desired.isEmpty()) {
            var prefs = desired.stream()
                    .map(topic -> TopicPreferenceEntity.builder().topic(topic).user(user).build())
                    .toList();
            topicPreferenceRepository.saveAll(prefs);
        }
    }

    private String applyTopicBindings(String userId, Set<String> desired) {
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

        userTopics.put(userId, Set.copyOf(desired));
        amqpAdmin.purgeQueue(q, false);

        return q;
    }
}