package com.widebot.economiccalendarbot.handler;

import com.widebot.economiccalendarbot.service.ScreenshotService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import static com.widebot.economiccalendarbot.utils.MessageBuilder.*;

@Component
public class CallBackHandler {

    private final ScreenshotService screenshotService;

    public CallBackHandler(ScreenshotService screenshotService) {
        this.screenshotService = screenshotService;
    }

    public Object handle(CallbackQuery callback) {
        String data = callback.getData();
        Long chatId = callback.getMessage().getChatId();

        if (data.startsWith("screenshot_")) {
            String pair = data.replace("screenshot_", "");
            return screenshot(chatId, pair, screenshotService); // SendPhoto
        }

        return simple(chatId, "❌ Callback non riconosciuto.");
    }
}
