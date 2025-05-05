package com.widebot.economiccalendarbot.bot;

import com.widebot.economiccalendarbot.service.EconomicEventService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

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
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String msg = update.getMessage().getText().trim().toLowerCase();
            String risposta;

            switch (msg) {
                case "/start" -> risposta = "👋 Benvenuto! Scrivi /help per i comandi.";
                case "/oggi" -> risposta = economicEventService.getCalendarioDiOggi();
                case "/usa" -> risposta = economicEventService.getEventiPerValuta("USD");
                case "/eur" -> risposta = economicEventService.getEventiPerValuta("EUR");
                case "/top" -> risposta = economicEventService.getEventiAdAltoImpatto();
                case "/help" -> risposta = "Comandi: /oggi /usa /eur /top /help";
                default -> risposta = "❌ Comando non riconosciuto.";
            }

            SendMessage reply = new SendMessage(chatId.toString(), risposta);
            reply.setParseMode("Markdown");
            return reply;
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
