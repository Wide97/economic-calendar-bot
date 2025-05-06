package com.widebot.economiccalendarbot.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardFactory {

    public SendMessage screenshotKeyboard(Long chatId) {
        SendMessage message = new SendMessage(chatId.toString(), "📸 Seleziona un asset:");
        message.setParseMode("Markdown");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(button("EURUSD"), button("GBPUSD"), button("XAUUSD")));
        rows.add(List.of(button("BTCUSD"), button("US500"), button("US100")));
        rows.add(List.of(button("GER40")));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);
        return message;
    }

    private InlineKeyboardButton button(String pair) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(pair);
        btn.setCallbackData("screenshot_" + pair);
        return btn;
    }
}
