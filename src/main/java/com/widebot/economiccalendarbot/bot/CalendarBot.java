package com.widebot.economiccalendarbot.bot;

import com.widebot.economiccalendarbot.service.EconomicEventService;
import com.widebot.economiccalendarbot.service.LottoCalculatorService;
import com.widebot.economiccalendarbot.service.ScreenshotService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Component
public class CalendarBot extends TelegramWebhookBot {

    private static final Logger logger = LoggerFactory.getLogger(CalendarBot.class);

    private final EconomicEventService economicEventService;
    private final LottoCalculatorService lottoCalculatorService;
    private final ScreenshotService screenshotService;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.webhookPath}")
    private String webhookPath;

    @Value("${bot.chatids.path}")
    private String chatIdsPath;

    public CalendarBot(EconomicEventService economicEventService,
                       LottoCalculatorService lottoCalculatorService,
                       ScreenshotService screenshotService) {
        this.economicEventService = economicEventService;
        this.lottoCalculatorService = lottoCalculatorService;
        this.screenshotService = screenshotService;
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Rome"));
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update == null) return null;

        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String msg = update.getMessage().getText().trim();

            salvaChatIdSeNuovo(chatId);

            switch (msg.toLowerCase()) {
                case "/start":
                    return handleStartCommand(chatId);
                case "/help":
                    return helpMessage(chatId);
                case "/oggi":
                    return buildMessage(chatId, economicEventService.getCalendarioDiOggi());
                case "/usa":
                    return buildMessage(chatId, economicEventService.getEventiPerValuta("USD"));
                case "/eur":
                    return buildMessage(chatId, economicEventService.getEventiPerValuta("EUR"));
                case "/top":
                    return buildMessage(chatId, economicEventService.getEventiAdAltoImpatto());
                case "/lotto":
                    return helpLottoMessage(chatId);
                case "/screenshot":
                    return helpScreenshotMessage(chatId);
                default:
                    if (msg.toLowerCase().startsWith("/lotto ")) return handleLottoCommand(chatId, msg.split(" "));
                    if (msg.toLowerCase().startsWith("/screenshot ")) return handleScreenshotCommand(chatId, msg.split(" "));
                    return buildMessage(chatId, "❌ Comando non riconosciuto. Scrivi /help per vedere l'elenco.");
            }
        }

        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (data.startsWith("screenshot_")) {
                String pair = data.replace("screenshot_", "");
                return sendScreenshotByCallback(chatId, pair);
            }

            return buildMessage(chatId, "❌ Callback non riconosciuto.");
        }

        return null;
    }

    private BotApiMethod<?> handleLottoCommand(Long chatId, String[] parts) {
        if (parts.length != 5)
            return buildMessage(chatId, "❗ Formato comando errato. Usa: /lotto EURUSD 2000 1.5 15");
        try {
            String pair = parts[1];
            double capitale = Double.parseDouble(parts[2]);
            double rischio = Double.parseDouble(parts[3]);
            double sl = Double.parseDouble(parts[4]);
            String risposta = lottoCalculatorService.calcolaLotti(pair, capitale, rischio, sl);
            return buildMessage(chatId, risposta);
        } catch (NumberFormatException e) {
            return buildMessage(chatId, "⚠️ Parametri non validi. Esempio corretto: /lotto EURUSD 2000 1.5 15");
        }
    }

    private BotApiMethod<?> handleScreenshotCommand(Long chatId, String[] parts) {
        if (parts.length != 2) return buildMessage(chatId, "❗ Usa il formato: /screenshot EURUSD");
        String pair = parts[1].toUpperCase();
        return sendScreenshotByCallback(chatId, pair);
    }

    private BotApiMethod<?> sendScreenshotByCallback(Long chatId, String pair) {
        try {
            String imageUrl = screenshotService.getScreenshotUrlForPair(pair);
            if (imageUrl == null) return buildMessage(chatId, "⚠️ Pair non supportato: " + pair);
            SendPhoto photo = new SendPhoto();
            photo.setChatId(chatId.toString());
            photo.setPhoto(new InputFile(imageUrl));
            execute(photo);
            return null;
        } catch (Exception e) {
            logger.error("Errore screenshot {}: {}", pair, e.getMessage());
            return buildMessage(chatId, "❌ Errore nel recupero dello screenshot per " + pair);
        }
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void invioEventiAdAltoImpattoATutti() {
        String messaggio = economicEventService.getEventiAdAltoImpatto();
        Set<Long> chatIds = getChatIdsFromFile();
        for (Long chatId : chatIds) {
            try {
                SendMessage msg = new SendMessage(chatId.toString(), messaggio);
                msg.setParseMode("Markdown");
                execute(msg);
                logger.info("✅ Evento inviato a: {}", chatId);
            } catch (Exception e) {
                logger.error("❌ Errore invio a {}: {}", chatId, e.getMessage());
            }
        }
    }

    private Set<Long> getChatIdsFromFile() {
        Path path = Paths.get(chatIdsPath);
        if (!Files.exists(path)) return new HashSet<>();
        try {
            return new HashSet<>(Files.readAllLines(path).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .toList());
        } catch (IOException e) {
            logger.error("Errore lettura chat_ids.txt: {}", e.getMessage());
            return new HashSet<>();
        }
    }

    private void salvaChatIdSeNuovo(Long chatId) {
        Set<Long> esistenti = getChatIdsFromFile();
        if (!esistenti.contains(chatId)) {
            try {
                Files.write(Paths.get(chatIdsPath),
                        Collections.singletonList(chatId.toString() + "\n"),
                        Files.exists(Paths.get(chatIdsPath)) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
            } catch (IOException e) {
                logger.error("Errore scrittura chatId {}: {}", chatId, e.getMessage());
            }
        }
    }

    private SendMessage helpScreenshotMessage(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("📸 Seleziona un asset per ricevere lo screenshot del grafico M15:");
        message.setParseMode("Markdown");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(createButton("EURUSD"), createButton("GBPUSD"), createButton("XAUUSD")));
        rows.add(List.of(createButton("BTCUSD"), createButton("US500"), createButton("US100")));
        rows.add(List.of(createButton("GER40")));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);
        return message;
    }

    private InlineKeyboardButton createButton(String pair) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(pair);
        button.setCallbackData("screenshot_" + pair);
        return button;
    }

    private SendMessage handleStartCommand(Long chatId) {
        SendMessage msg = buildMessage(chatId, """
                👋 *Benvenuto nel Calendario Economico Bot!*

                ✅ Tutto è pronto.
                Usa /help per vedere i comandi disponibili.
                """);
        msg.setReplyMarkup(new ReplyKeyboardRemove(true));
        return msg;
    }

    private SendMessage helpMessage(Long chatId) {
        return buildMessage(chatId, """
                📌 *Comandi disponibili:*

                /oggi - Eventi economici previsti per *oggi*  
                /usa - Eventi in *dollari* (USD)  
                /eur - Eventi in *euro* (EUR)  
                /top - Eventi ad *alto impatto* ⭐⭐⭐  
                /lotto - Calcolo lotti consigliati  
                /screenshot - Screenshot grafico M15  
                /help - Questo elenco
                """);
    }

    private SendMessage helpLottoMessage(Long chatId) {
        return buildMessage(chatId, """
                🧮 *Calcolatore Lotto*

                ✏️ Formato comando:
                `/lotto <pair> <capitale> <rischio%> <stoploss pip>`

                📌 Esempio:
                `/lotto EURUSD 2000 1.5 15`
                """);
    }

    private SendMessage buildMessage(Long chatId, String text) {
        SendMessage msg = new SendMessage(chatId.toString(), text);
        msg.setParseMode("Markdown");
        return msg;
    }

    @Override public String getBotUsername() { return botName; }
    @Override public String getBotToken() { return botToken; }
    @Override public String getBotPath() { return webhookPath; }
}
