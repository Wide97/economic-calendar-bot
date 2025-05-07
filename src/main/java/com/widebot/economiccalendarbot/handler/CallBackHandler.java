package com.widebot.economiccalendarbot.handler;

import com.widebot.economiccalendarbot.model.LottoSession;
import com.widebot.economiccalendarbot.model.ScreenshotSession;
import com.widebot.economiccalendarbot.service.ScreenshotService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import static com.widebot.economiccalendarbot.utils.MessageBuilder.simple;
import static com.widebot.economiccalendarbot.utils.MessageBuilder.screenshot;

@Component
public class CallBackHandler {

    private final ScreenshotService screenshotService;
    private final LottoSessionManager lottoSessionManager;
    private final ScreenshotSessionManager screenshotSessionManager;
    private final KeyboardFactory keyboardFactory;

    public CallBackHandler(
            ScreenshotService screenshotService,
            LottoSessionManager lottoSessionManager,
            ScreenshotSessionManager screenshotSessionManager,
            KeyboardFactory keyboardFactory) {
        this.screenshotService = screenshotService;
        this.lottoSessionManager = lottoSessionManager;
        this.screenshotSessionManager = screenshotSessionManager;
        this.keyboardFactory = keyboardFactory;
    }

    public Object handle(CallbackQuery callback) {
        String data = callback.getData();
        Long chatId = callback.getMessage().getChatId();

        // Screenshot: Step 1 - scelta pair
        if (data.startsWith("screenshot_pair_")) {
            String pair = data.replace("screenshot_pair_", "");
            ScreenshotSession session = screenshotSessionManager.getOrCreate(chatId);
            session.setPair(pair);
            return keyboardFactory.screenshotTimeframeKeyboard(chatId);
        }

        // Screenshot: Step 2 - scelta timeframe
        if (data.startsWith("screenshot_tf_")) {
            String timeframe = data.replace("screenshot_tf_", "");
            ScreenshotSession session = screenshotSessionManager.getOrCreate(chatId);
            if (session.getPair() == null) {
                return simple(chatId, "❌ Pair non selezionato. Usa /screenshot per riprovare.");
            }
            session.setTimeframe(timeframe);
            String imageUrl = screenshotService.getScreenshotUrl(session.getPair(), session.getTimeframe());
            screenshotSessionManager.clear(chatId);
            return screenshot(chatId, imageUrl);
        }

        // Lotto flow
        if (data.startsWith("lotto_pair_")) {
            String pair = data.replace("lotto_pair_", "");
            LottoSession session = lottoSessionManager.getOrCreate(chatId);
            session.setPair(pair);
            return simple(chatId, "✏️ Inserisci il *capitale* in EUR:");
        }

        return simple(chatId, "❌ Callback non riconosciuto.");
    }
}
