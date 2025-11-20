package com.sanch.appNotify.command;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRecordRepository extends JpaRepository<MessageRecordEntity, Long> {
}
