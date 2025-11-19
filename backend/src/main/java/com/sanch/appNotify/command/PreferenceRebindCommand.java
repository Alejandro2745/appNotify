package com.sanch.appNotify.command;

import java.util.Map;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.sanch.appNotify.core.PreferenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PreferenceRebindCommand implements CommandLineRunner {
    private final PreferenceService preferenceService;

    @Override
    public void run(String... args) {
        Map<String, Set<String>> stored = preferenceService.fetchAllPreferences();
        if (stored.isEmpty()) {
            log.info("No hay preferencias persistidas que restaurar");
            return;
        }

        log.info("Restaurando {} usuarios desde Postgres", stored.size());
        stored.forEach((userId, topics) -> {
            preferenceService.ensureDmQueue(userId);
            preferenceService.reapplyBindings(userId, topics);
            log.info("Bindings reestablecidos para {} ({} topics)", userId, topics.size());
        });
    }
}