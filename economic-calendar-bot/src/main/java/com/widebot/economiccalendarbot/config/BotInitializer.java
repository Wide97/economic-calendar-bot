package com.widebot.economiccalendarbot.config;

import com.widebot.economiccalendarbot.bot.CalendarBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotInitializer {

    @Bean
    public TelegramBotsApi telegramBotsApi(CalendarBot calendarBot) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(calendarBot);
        System.out.println("✅ Bot registrato correttamente via TelegramBotsApi");
        return botsApi;
    }
}
