package com.widebot.economiccalendarbot.config;

import com.widebot.economiccalendarbot.bot.CalendarBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotInitializer {

    @Value("${bot.webhookUrl}")
    private String webhookUrl;

    @Bean
    public TelegramBotsApi telegramBotsApi(CalendarBot calendarBot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(calendarBot, new SetWebhook(webhookUrl));
        return api;
    }
}
