package com.widebot.economiccalendarbot.handler;

import com.widebot.economiccalendarbot.model.LottoSession;
import com.widebot.economiccalendarbot.service.ScreenshotService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import static com.widebot.economiccalendarbot.utils.MessageBuilder.simple;
import static com.widebot.economiccalendarbot.utils.MessageBuilder.screenshot;

@Component
public class CallBackHandler {

    private final ScreenshotService screenshotService;
    private final LottoSessionManager sessionManager;

    public CallBackHandler(ScreenshotService screenshotService, LottoSessionManager sessionManager) {
        this.screenshotService = screenshotService;
        this.sessionManager = sessionManager;
    }

    public Object handle(CallbackQuery callback) {
        String data = callback.getData();
        Long chatId = callback.getMessage().getChatId();

        if (data.startsWith("screenshot_")) {
            String pair = data.replace("screenshot_", "");
            return screenshot(chatId, pair, screenshotService);
        }

        if (data.startsWith("lotto_pair_")) {
            String pair = data.replace("lotto_pair_", "");
            LottoSession session = sessionManager.getOrCreate(chatId);
            session.setPair(pair);
            return simple(chatId, "✏️ Inserisci il *capitale* in EUR:");
        }

        return simple(chatId, "❌ Callback non riconosciuto.");
    }
}
