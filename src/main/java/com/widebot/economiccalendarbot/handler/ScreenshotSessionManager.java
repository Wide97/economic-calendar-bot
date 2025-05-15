package com.widebot.economiccalendarbot.handler;

import com.widebot.economiccalendarbot.model.ScreenshotSession;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ScreenshotSessionManager {

    private final Map<Long, ScreenshotSession> sessions = new HashMap<>();

    public ScreenshotSession getOrCreate(Long chatId) {
        return sessions.computeIfAbsent(chatId, id -> new ScreenshotSession());
    }

    public void clear(Long chatId) {
        sessions.remove(chatId);
    }
}
