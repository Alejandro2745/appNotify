package com.sanch.appNotify.query;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageHistoryRepository extends MongoRepository<MessageDocument, String> {
}