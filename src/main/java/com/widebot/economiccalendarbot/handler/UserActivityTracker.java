package com.widebot.economiccalendarbot.handler;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserActivityTracker {
    private final Map<Long, Long> lastSeenMap = new ConcurrentHashMap<>();
    private static final long TIMEOUT_MS = 120_000; // 1 minuto

    public boolean isInactive(Long chatId) {
        Long last = lastSeenMap.get(chatId);
        return last != null && (System.currentTimeMillis() - last > TIMEOUT_MS);
    }

    public void updateActivity(Long chatId) {
        lastSeenMap.put(chatId, System.currentTimeMillis());
    }

    public void clear(Long chatId) {
        lastSeenMap.remove(chatId);
    }
}
