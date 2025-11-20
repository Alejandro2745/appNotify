package com.sanch.appNotify.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageHistoryQueryService {
    private final MongoTemplate template;

    public Page<MessageDocument> search(Optional<MessageType> type,
                                        Optional<String> from,
                                        Optional<String> to,
                                        Optional<String> topic,
                                        Pageable pageable) {
        List<Criteria> predicates = new ArrayList<>();
        type.ifPresent(t -> predicates.add(Criteria.where("type").is(t)));
        from.ifPresent(f -> predicates.add(Criteria.where("fromUser").is(f)));
        to.ifPresent(t -> predicates.add(Criteria.where("toUser").is(t)));
        topic.ifPresent(tp -> predicates.add(Criteria.where("topic").is(tp.startsWith("notify.") ? tp : "notify." + tp)));

        Query query = new Query();
        if (!predicates.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(predicates));
        }
        query.with(pageable);

        List<MessageDocument> results = template.find(query, MessageDocument.class);
        return PageableExecutionUtils.getPage(results, pageable,
                () -> template.count(Query.of(query).limit(-1).skip(-1), MessageDocument.class));
    }
}