package com.sanch.appNotify.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanch.appNotify.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class DynamicListenerConfig {

  private final ConnectionFactory connectionFactory;
  private final AmqpAdmin amqpAdmin;
  private final SseHub sse;

  private final ConcurrentHashMap<String, MessageListenerContainer> userDmContainers = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, MessageListenerContainer> userPrefContainers = new ConcurrentHashMap<>();

  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Crea (si no existen) las colas del usuario y arranca listeners que envían al SSE del usuario.
   * Llama este método cuando se abre el stream: /api2/stream/{userId}
   */
  public void ensureUserConsumers(String userId) {
    // --- DM (direct) ---
    String dmQueue = "user." + userId + ".queue";
    declareDmQueueAndBinding(userId, dmQueue);
    userDmContainers.computeIfAbsent(userId, id ->
        startContainer(dmQueue, payload -> sse.send(userId, payload))
    );

    // --- Preferencias (topic) ---
    String prefsQueue = "user." + userId + ".prefs.queue";
    declareQueue(prefsQueue); // los bindings a notify.exchange los hace PreferenceService#setTopicPrefs
    userPrefContainers.computeIfAbsent(userId, id ->
        startContainer(prefsQueue, payload -> sse.send(userId, payload))
    );
  }

  /* ===================== Declaraciones ===================== */

  private void declareQueue(String q) {
    amqpAdmin.declareQueue(QueueBuilder.durable(q).build());
  }

  private void declareDmQueueAndBinding(String userId, String q) {
    // Cola durable para DMs del usuario
    amqpAdmin.declareQueue(QueueBuilder.durable(q).build());

    // Binding al exchange direct con routing key user.<id>
    DirectExchange dmEx = new DirectExchange(RabbitConfig.DM_EX, true, false);
    amqpAdmin.declareExchange(dmEx);
    Binding b = BindingBuilder.bind(new Queue(q))
        .to(dmEx)
        .with("user." + userId);
    amqpAdmin.declareBinding(b);
  }

  /* ===================== Listener container ===================== */

  private MessageListenerContainer startContainer(String queue, Consumer<Object> onMsg) {
    SimpleMessageListenerContainer c = new SimpleMessageListenerContainer(connectionFactory);
    c.setQueueNames(queue);
    c.setMessageListener(message -> {
      try {
        Object payload = decodeMessage(message);
        onMsg.accept(payload);
      } catch (Exception e) {
        onMsg.accept("<<mensaje no decodificado>>");
      }
    });
    c.start();
    return c;
  }

  /* ===================== Decodificación ===================== */

  private Object decodeMessage(org.springframework.amqp.core.Message message) throws Exception {
    String contentType = message.getMessageProperties().getContentType();
    byte[] body = message.getBody();

    // Si viene marcado como JSON, deserializa a Map
    if (contentType != null && contentType.toLowerCase().contains("json")) {
      return objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
    }

    // Fallback: intenta como texto UTF-8 y parsea JSON; si no es JSON, devuelve String
    String s = new String(body, StandardCharsets.UTF_8);
    try {
      return objectMapper.readValue(s, new TypeReference<Map<String, Object>>() {});
    } catch (Exception ignore) {
      return s;
    }
  }
}
