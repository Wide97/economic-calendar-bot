package com.widebot.economiccalendarbot.config;

import com.widebot.economiccalendarbot.bot.CalendarBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotInitializer {

    private static final Logger log = LoggerFactory.getLogger(BotInitializer.class);

    @Value("${bot.webhookUrl}")
    private String webhookUrl;

    @Bean
    public SetWebhook setWebhook() {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("⚠️ Il webhookUrl è vuoto! Il bot potrebbe non registrarsi correttamente.");
        }
        return SetWebhook.builder().url(webhookUrl).build();
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(CalendarBot calendarBot, SetWebhook setWebhook) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(calendarBot, setWebhook);
        log.info("✅ Bot Telegram registrato con webhook: {}", webhookUrl);
        return api;
    }
}
