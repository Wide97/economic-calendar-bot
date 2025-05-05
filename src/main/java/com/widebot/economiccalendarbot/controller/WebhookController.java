package com.widebot.economiccalendarbot.controller;

import com.widebot.economiccalendarbot.bot.CalendarBot;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class WebhookController {

    private final CalendarBot calendarBot;

    public WebhookController(CalendarBot calendarBot) {
        this.calendarBot = calendarBot;
    }

    @PostMapping("${bot.webhookPath}") // deve essere così, con POST
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return calendarBot.onWebhookUpdateReceived(update);
    }
}

