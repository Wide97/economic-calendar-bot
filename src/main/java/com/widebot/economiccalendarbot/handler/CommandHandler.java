package com.widebot.economiccalendarbot.handler;

import com.widebot.economiccalendarbot.service.EconomicEventService;
import com.widebot.economiccalendarbot.service.LottoCalculatorService;
import com.widebot.economiccalendarbot.service.ScreenshotService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import static com.widebot.economiccalendarbot.utils.MessageBuilder.*;

@Component
public class CommandHandler {

    private final EconomicEventService economicEventService;
    private final LottoCalculatorService lottoCalculatorService;
    private final ScreenshotService screenshotService;
    private final KeyboardFactory keyboardFactory;
    private final ChatIdRepository chatIdRepository;

    public CommandHandler(EconomicEventService economicEventService,
                          LottoCalculatorService lottoCalculatorService,
                          ScreenshotService screenshotService,
                          KeyboardFactory keyboardFactory,
                          ChatIdRepository chatIdRepository) {
        this.economicEventService = economicEventService;
        this.lottoCalculatorService = lottoCalculatorService;
        this.screenshotService = screenshotService;
        this.keyboardFactory = keyboardFactory;
        this.chatIdRepository = chatIdRepository;
    }

    public Object handle(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText().trim();
        chatIdRepository.saveIfNew(chatId);

        return switch (text.toLowerCase()) {
            case "/start" -> start(chatId);
            case "/help" -> help(chatId);
            case "/oggi" -> simple(chatId, economicEventService.getCalendarioDiOggi());
            case "/usa" -> simple(chatId, economicEventService.getEventiPerValuta("USD"));
            case "/eur" -> simple(chatId, economicEventService.getEventiPerValuta("EUR"));
            case "/top" -> simple(chatId, economicEventService.getEventiAdAltoImpatto());
            case "/lotto" -> lottoHelp(chatId);
            case "/screenshot" -> keyboardFactory.screenshotKeyboard(chatId);
            default -> handleDynamic(chatId, text);
        };
    }

    private Object handleDynamic(Long chatId, String text) {
        if (text.toLowerCase().startsWith("/lotto ")) {
            String[] parts = text.split(" ");
            return lottoResult(chatId, parts, lottoCalculatorService);
        }

        if (text.toLowerCase().startsWith("/screenshot ")) {
            String[] parts = text.split(" ");
            if (parts.length == 2) {
                return screenshot(chatId, parts[1], screenshotService);
            }
        }

        return simple(chatId, "❌ Comando non riconosciuto. Scrivi /help per vedere l'elenco.");
    }
}
