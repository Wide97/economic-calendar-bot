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
        rows.add(List.of(screenshotBtn("EURUSD"), screenshotBtn("GBPUSD"), screenshotBtn("XAUUSD")));
        rows.add(List.of(screenshotBtn("BTCUSD"), screenshotBtn("US500"), screenshotBtn("US100")));
        rows.add(List.of(screenshotBtn("GER40")));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);
        return message;
    }

    public SendMessage screenshotTimeframeKeyboard(Long chatId) {
        SendMessage message = new SendMessage(chatId.toString(), "⏱ Seleziona il *timeframe*:");
        message.setParseMode("Markdown");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(timeframeBtn("15", "M15"), timeframeBtn("60", "H1")));
        rows.add(List.of(timeframeBtn("240", "H4"), timeframeBtn("1D", "D1")));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);
        return message;
    }

    public SendMessage lottoPairKeyboard(Long chatId) {
        SendMessage message = new SendMessage(chatId.toString(), "📊 *Calcolo Lotto*\n\nScegli il *pair*:");
        message.setParseMode("Markdown");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(lottoBtn("EURUSD"), lottoBtn("GBPUSD"), lottoBtn("XAUUSD")));
        rows.add(List.of(lottoBtn("BTCUSD"), lottoBtn("US500"), lottoBtn("US100")));
        rows.add(List.of(lottoBtn("GER40")));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);
        return message;
    }

    private InlineKeyboardButton screenshotBtn(String pair) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(pair);
        btn.setCallbackData("screenshot_pair_" + pair);
        return btn;
    }

    private InlineKeyboardButton timeframeBtn(String value, String label) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(label);
        btn.setCallbackData("screenshot_tf_" + value);
        return btn;
    }

    private InlineKeyboardButton lottoBtn(String pair) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(pair);
        btn.setCallbackData("lotto_pair_" + pair);
        return btn;
    }
}
