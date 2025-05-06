package com.widebot.economiccalendarbot.controller;

import com.widebot.economiccalendarbot.bot.CalendarBot;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class WebhookController {

    private final CalendarBot calendarBot;

    public WebhookController(CalendarBot calendarBot) {
        this.calendarBot = calendarBot;
    }

    @PostMapping("${bot.webhookPath}") // es. /webhook
    public Object onUpdateReceived(@RequestBody Update update) {
        return calendarBot.onWebhookUpdateReceived(update);
    }
}


