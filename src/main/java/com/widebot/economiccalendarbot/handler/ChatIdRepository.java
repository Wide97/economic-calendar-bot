package com.widebot.economiccalendarbot.handler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ChatIdRepository {

    @Value("${bot.chatids.path}")
    private String path;

    public void saveIfNew(Long chatId) {
        Set<Long> existing = load();
        if (!existing.contains(chatId)) {
            try {
                Files.write(Paths.get(path),
                        (chatId + "\n").getBytes(),
                        Files.exists(Paths.get(path)) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Set<Long> load() {
        Path filePath = Paths.get(path);
        if (!Files.exists(filePath)) return new HashSet<>();
        try {
            return Files.readAllLines(filePath).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            return new HashSet<>();
        }
    }
}
