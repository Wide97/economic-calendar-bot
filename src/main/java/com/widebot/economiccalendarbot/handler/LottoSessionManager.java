package com.widebot.economiccalendarbot.handler;

import com.widebot.economiccalendarbot.model.LottoSession;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LottoSessionManager {
    private final Map<Long, LottoSession> sessionMap = new HashMap<>();

    public LottoSession getOrCreate(Long chatId) {
        return sessionMap.computeIfAbsent(chatId, id -> new LottoSession());
    }

    public void clear(Long chatId) {
        sessionMap.remove(chatId);
    }
}
