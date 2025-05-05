package com.widebot.economiccalendarbot.bot;

import com.widebot.economiccalendarbot.service.EconomicEventService;
import com.widebot.economiccalendarbot.service.LottoCalculatorService;
import com.widebot.economiccalendarbot.service.ScreenshotService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Bot Telegram per calendario economico + calcolatore di lotto + screenshot grafici M15.
 * Risponde ai comandi tramite webhook e invia ogni giorno automaticamente gli eventi economici ad alto impatto.
 */
@Component
public class CalendarBot extends TelegramWebhookBot {

    @Autowired private EconomicEventService economicEventService;
    @Autowired private LottoCalculatorService lottoCalculatorService;
    @Autowired private ScreenshotService screenshotService;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.webhookPath}")
    private String webhookPath;

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Rome"));
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update == null || !update.hasMessage() || !update.getMessage().hasText()) return null;

        Long chatId = update.getMessage().getChatId();
        String msg = update.getMessage().getText().trim().toLowerCase();

        switch (msg) {
            case "/start":
                return handleStartCommand(chatId);

            case "/help":
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

            case "/oggi":
                return buildMessage(chatId, economicEventService.getCalendarioDiOggi());

            case "/usa":
                return buildMessage(chatId, economicEventService.getEventiPerValuta("USD"));

            case "/eur":
                return buildMessage(chatId, economicEventService.getEventiPerValuta("EUR"));

            case "/top":
                return buildMessage(chatId, economicEventService.getEventiAdAltoImpatto());

            case "/lotto":
                return buildMessage(chatId, """
                    🧮 *Calcolatore Lotto*

                    ✏️ Formato comando:
                    `/lotto <pair> <capitale> <rischio%> <stoploss pip>`

                    📌 Esempio:
                    `/lotto EURUSD 2000 1.5 15`

                    👉 Significato:
                    - *pair*: strumento (es: EURUSD, XAUUSD, US500, ecc.)
                    - *capitale*: capitale in EUR
                    - *rischio%*: rischio per trade
                    - *SL pip*: distanza dello stop loss
                    """);

            case "/screenshot":
                return buildMessage(chatId, """
                    📸 *Screenshot Grafico*

                    ✏️ Usa il comando così:
                    `/screenshot EURUSD`

                    🔁 Supportati: EURUSD, GBPUSD, XAUUSD, BTCUSD, US500, US100, GER40
                    """);

            default:
                if (msg.startsWith("/lotto ")) {
                    String[] parts = msg.split(" ");
                    if (parts.length == 5) {
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
                    } else {
                        return buildMessage(chatId, "❗ Formato comando errato. Usa: /lotto EURUSD 2000 1.5 15");
                    }
                }

                if (msg.startsWith("/screenshot ")) {
                    String[] parts = msg.split(" ");
                    if (parts.length == 2) {
                        String pair = parts[1].toUpperCase();
                        try {
                            String imageUrl = screenshotService.getScreenshotUrlForPair(pair);
                            SendPhoto photo = new SendPhoto();
                            photo.setChatId(chatId.toString());
                            photo.setPhoto(new InputFile(imageUrl));
                            execute(photo);
                            return null;
                        } catch (Exception e) {
                            return buildMessage(chatId, "❌ Errore nel recupero dello screenshot per " + pair);
                        }
                    } else {
                        return buildMessage(chatId, "❗ Usa il formato: /screenshot EURUSD");
                    }
                }

                return buildMessage(chatId, "❌ Comando non riconosciuto. Scrivi /help per vedere l'elenco.");
        }
    }

    /**
     * Esegue ogni giorno alle 8 del mattino e invia gli eventi ad alto impatto.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void invioEventiAdAltoImpattoATutti() {
        String messaggio = economicEventService.getEventiAdAltoImpatto();
        List<Long> chatIds = getChatIdsFromFile();

        for (Long chatId : chatIds) {
            SendMessage msg = new SendMessage(chatId.toString(), messaggio);
            msg.setParseMode("Markdown");
            try {
                execute(msg);
                System.out.println("✅ Evento inviato a: " + chatId);
            } catch (Exception e) {
                System.out.println("❌ Errore invio a: " + chatId);
            }
        }
    }

    private List<Long> getChatIdsFromFile() {
        Path path = Paths.get("src/main/resources/chat_ids.txt");
        if (!Files.exists(path)) return new ArrayList<>();
        try {
            return Files.readAllLines(path).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .distinct()
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void salvaChatIdSeNuovo(Long chatId) {
        List<Long> esistenti = getChatIdsFromFile();
        if (!esistenti.contains(chatId)) {
            try {
                Files.write(Paths.get("src/main/resources/chat_ids.txt"),
                        Collections.singletonList(chatId.toString() + "\n"),
                        Files.exists(Paths.get("src/main/resources/chat_ids.txt"))
                                ? StandardOpenOption.APPEND
                                : StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private SendMessage handleStartCommand(Long chatId) {
        SendMessage msg = buildMessage(chatId, """
                👋 *Benvenuto nel Calendario Economico Bot!*

                ✅ Tutto è pronto.
                Usa /help per vedere i comandi disponibili.
                """);
        msg.setReplyMarkup(new ReplyKeyboardRemove(true));
        salvaChatIdSeNuovo(chatId);
        return msg;
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
