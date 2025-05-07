package com.widebot.economiccalendarbot.handler;

import com.widebot.economiccalendarbot.model.LottoSession;
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
    private final LottoSessionManager sessionManager;

    public CommandHandler(EconomicEventService economicEventService,
                          LottoCalculatorService lottoCalculatorService,
                          ScreenshotService screenshotService,
                          KeyboardFactory keyboardFactory,
                          ChatIdRepository chatIdRepository,
                          LottoSessionManager sessionManager) {
        this.economicEventService = economicEventService;
        this.lottoCalculatorService = lottoCalculatorService;
        this.screenshotService = screenshotService;
        this.keyboardFactory = keyboardFactory;
        this.chatIdRepository = chatIdRepository;
        this.sessionManager = sessionManager;
    }

    public Object handle(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText().trim();
        chatIdRepository.saveIfNew(chatId);

        // Flusso guidato lotto
        LottoSession session = sessionManager.getOrCreate(chatId);

        if ("/lotto".equalsIgnoreCase(text)) {
            return keyboardFactory.lottoPairKeyboard(chatId);
        }

        if (session.getPair() != null && session.getCapitale() == null) {
            try {
                session.setCapitale(Double.parseDouble(text));
                return simple(chatId, "✏️ Inserisci il *rischio* in percentuale:");
            } catch (NumberFormatException e) {
                return simple(chatId, "❌ Capitale non valido. Inserisci un numero (es: 2000).");
            }
        }

        if (session.getCapitale() != null && session.getRischio() == null) {
            try {
                session.setRischio(Double.parseDouble(text));
                return simple(chatId, "✏️ Inserisci lo *stop loss* (in pip o punti):");
            } catch (NumberFormatException e) {
                return simple(chatId, "❌ Rischio non valido. Inserisci un numero (es: 1.5).");
            }
        }

        if (session.getRischio() != null && session.getStopLoss() == null) {
            try {
                session.setStopLoss(Double.parseDouble(text));
                String result = lottoCalculatorService.calcolaLotti(
                        session.getPair(),
                        session.getCapitale(),
                        session.getRischio(),
                        session.getStopLoss()
                );
                sessionManager.clear(chatId);
                return simple(chatId, result);
            } catch (NumberFormatException e) {
                return simple(chatId, "❌ Stop loss non valido. Inserisci un numero (es: 15).");
            }
        }

        return switch (text.toLowerCase()) {
            case "/start" -> start(chatId);
            case "/help" -> help(chatId);
            case "/oggi" -> keyboardFactory.newsLevelKeyboard(chatId); // 🔁 QUI il cambiamento
            case "/usa" -> simple(chatId, economicEventService.getEventiPerValuta("USD"));
            case "/eur" -> simple(chatId, economicEventService.getEventiPerValuta("EUR"));
            case "/top" -> simple(chatId, economicEventService.getEventiAdAltoImpatto());
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
