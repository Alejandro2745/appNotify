package com.sanch.appNotify.query;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messageHistory")
public class MessageDocument {
    @Id
    private String id;
    private MessageType type;
    private String fromUser;
    private String toUser;
    private String topic;
    private String text;
    private String payload;
    private Instant createdAt;
}