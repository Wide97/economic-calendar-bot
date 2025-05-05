package com.widebot.economiccalendarbot.bot;

import com.widebot.economiccalendarbot.service.EconomicEventService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

/**
 * Bot Telegram collegato al calendario economico.
 * Usa la modalità webhook e risponde ai comandi ricevuti via Telegram.
 */
@Component
public class CalendarBot extends TelegramWebhookBot {

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
                    /help - Questo elenco
                    """);
            case "/oggi" -> buildMessage(chatId, economicEventService.getCalendarioDiOggi());
            case "/usa" -> buildMessage(chatId, economicEventService.getEventiPerValuta("USD"));
            case "/eur" -> buildMessage(chatId, economicEventService.getEventiPerValuta("EUR"));
            case "/top" -> buildMessage(chatId, economicEventService.getEventiAdAltoImpatto());
            default -> buildMessage(chatId, "❌ Comando non riconosciuto. Scrivi /help per vedere i comandi disponibili.");
        };
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
