package com.sanch.appNotify.command;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicPreferenceRepository extends JpaRepository<TopicPreferenceEntity, Long> {
    List<TopicPreferenceEntity> findByUser_UserId(String userId);
    void deleteByUser_UserId(String userId);
}