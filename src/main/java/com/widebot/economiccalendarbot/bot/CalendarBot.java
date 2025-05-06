package com.widebot.economiccalendarbot.bot;

import com.widebot.economiccalendarbot.handler.CallBackHandler;
import com.widebot.economiccalendarbot.handler.CommandHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.TimeZone;

/**
 * Bot Telegram principale. Inoltra i comandi testuali e le callback ai rispettivi handler.
 */
@Component
public class CalendarBot extends TelegramWebhookBot {

    private final CommandHandler commandHandler;
    private final CallBackHandler callBackHandler;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.webhookPath}")
    private String webhookPath;

    public CalendarBot(CommandHandler commandHandler, CallBackHandler callBackHandler) {
        this.commandHandler = commandHandler;
        this.callBackHandler = callBackHandler;
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Rome"));
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update == null) return null;

        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                Object result = commandHandler.handle(update.getMessage());
                return tryExecuteOrReturn(result);
            }

            if (update.hasCallbackQuery()) {
                Object result = callBackHandler.handle(update.getCallbackQuery());
                return tryExecuteOrReturn(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Esegue direttamente comandi come SendPhoto (che non sono BotApiMethod),
     * oppure li ritorna se sono BotApiMethod.
     */
    private BotApiMethod<?> tryExecuteOrReturn(Object response) {
        try {
            if (response instanceof BotApiMethod<?> botMethod) {
                return botMethod;
            } else if (response instanceof SendPhoto photo) {
                execute(photo); // invio diretto foto
            }
            // Puoi aggiungere qui altre azioni future (SendDocument, ecc.)
        } catch (Exception e) {
            System.err.println("❌ Errore durante l'invio: " + e.getMessage());
        }
        return null;
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
