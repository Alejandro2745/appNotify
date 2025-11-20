package com.sanch.appNotify.query;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class MessageHistoryController {
    private final MessageHistoryQueryService queryService;

    @GetMapping
    public Page<MessageDocument> list(
            @RequestParam Optional<MessageType> type,
            @RequestParam Optional<String> from,
            @RequestParam Optional<String> to,
            @RequestParam Optional<String> topic,
            @PageableDefault(size = 20)
            @SortDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
        return queryService.search(type, from, to, topic, pageable);
    }
}