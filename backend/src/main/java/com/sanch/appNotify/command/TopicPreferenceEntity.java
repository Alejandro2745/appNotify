package com.sanch.appNotify.command;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "topic_preferences", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "topic"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicPreferenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic", nullable = false, length = 255)
    private String topic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;
}