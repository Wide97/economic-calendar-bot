package com.widebot.economiccalendarbot.bot;

import com.widebot.economiccalendarbot.service.EconomicEventService;
import com.widebot.economiccalendarbot.service.LottoCalculatorService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Bot Telegram collegato al calendario economico.
 * Usa la modalità webhook e risponde ai comandi ricevuti via Telegram.
 */
@Component
public class CalendarBot extends TelegramWebhookBot {

    @Autowired
    LottoCalculatorService lottoCalculatorService;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.webhookPath}")
    private String webhookPath;

    private final EconomicEventService economicEventService;

    public CalendarBot(EconomicEventService economicEventService) {
        this.economicEventService = economicEventService;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update == null || !update.hasMessage() || !update.getMessage().hasText()) {
            System.out.println("❗ Update nullo o non testuale ricevuto.");
            return null;
        }

        Long chatId = update.getMessage().getChatId();
        String msg = update.getMessage().getText().trim().toLowerCase();

        System.out.println("📩 Messaggio ricevuto: " + msg + " da chatId: " + chatId);

        return switch (msg) {
            case "/start" -> handleStartCommand(chatId);

            case "/help" -> buildMessage(chatId, """
            📌 *Comandi disponibili:*

            /oggi - Eventi economici previsti per *oggi*  
            /usa - Eventi in *dollari* (USD)  
            /eur - Eventi in *euro* (EUR)  
            /top - Eventi ad *alto impatto* ⭐⭐⭐  
            /lotto - Calcolo dei lotti consigliati  
            /help - Questo elenco
            """);

            case "/oggi" -> buildMessage(chatId, economicEventService.getCalendarioDiOggi());
            case "/usa" -> buildMessage(chatId, economicEventService.getEventiPerValuta("USD"));
            case "/eur" -> buildMessage(chatId, economicEventService.getEventiPerValuta("EUR"));
            case "/top" -> buildMessage(chatId, economicEventService.getEventiAdAltoImpatto());
            case "/lotto" -> buildMessage(chatId, """
    🧮 *Calcolatore Lotto*

    ✏️ Formato comando:
    `/lotto <pair> <capitale> <rischio%> <stoploss pip>`

    📌 Esempio:
    `/lotto EURUSD 2000 1.5 15`

    👉 Significato:
    - *EURUSD*: strumento (supportati: EURUSD, GBPUSD, XAUUSD, BTCUSD, US500, US100, GER40)
    - *2000*: capitale del conto in EUR
    - *1.5*: percentuale di rischio per il trade
    - *15*: stop loss in pip o punti

    Il bot calcolerà il lotto corretto in base a questi parametri 📊
    """);


            default -> {
                if (msg.startsWith("/lotto ")) {
                    String[] parts = msg.split(" ");
                    if (parts.length == 5) {
                        String pair = parts[1];
                        try {
                            double capitale = Double.parseDouble(parts[2]);
                            double rischio = Double.parseDouble(parts[3]);
                            double sl = Double.parseDouble(parts[4]);
                            String risposta = lottoCalculatorService.calcolaLotti(pair, capitale, rischio, sl);
                            yield buildMessage(chatId, risposta); // <-- attenzione: serve `yield` nei blocchi con `switch`
                        } catch (NumberFormatException e) {
                            yield buildMessage(chatId, "⚠️ I valori devono essere numerici. Esempio: /lotto EURUSD 2000 1.5 15");
                        }
                    } else {
                        yield buildMessage(chatId, "❗ Formato non corretto. Usa: /lotto EURUSD 2000 1.5 15");
                    }
                }

                yield buildMessage(chatId, "❌ Comando non riconosciuto. Scrivi /help per vedere i comandi disponibili.");
            }
        };

    }


    @Scheduled(cron = "0 0 8 * * *")
   public void invioEventiAdAltoImpattoATutti() {
        String messaggio = economicEventService.getEventiAdAltoImpatto();
        List<Long> chatIds = getChatIdsFromFile();

        for (Long chatId : chatIds) {
            SendMessage msg = new SendMessage();
            msg.setChatId(chatId.toString());
            msg.setText(messaggio);
            msg.setParseMode("Markdown"); // opzionale, se vuoi usare grassetto, emoji, ecc.

            try {
                execute(msg);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("❌ Errore nell'invio automatico a: " + chatId);
            }

            System.out.println("✅ Evento inviato a: " + chatId);
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

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Rome"));
    }



    /**
     * Risposta personalizzata per il comando /start
     */
    private SendMessage handleStartCommand(Long chatId) {
        SendMessage msg = buildMessage(chatId, """
                👋 *Benvenuto nel Calendario Economico Bot!*

                Tutto è stato ripristinato ✅  
                Usa /help per vedere i comandi disponibili.
                """);
        msg.setReplyMarkup(new ReplyKeyboardRemove(true));
        return msg;
    }

    /**
     * Metodo riutilizzabile per creare risposte testuali Markdown
     */
    private SendMessage buildMessage(Long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(text);
        msg.setParseMode("Markdown");
        return msg;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return webhookPath;
    }
}
