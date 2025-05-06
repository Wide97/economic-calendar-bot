package com.widebot.economiccalendarbot.utils;

import com.widebot.economiccalendarbot.service.LottoCalculatorService;
import com.widebot.economiccalendarbot.service.ScreenshotService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

public class MessageBuilder {

    public static SendMessage simple(Long chatId, String text) {
        SendMessage msg = new SendMessage(chatId.toString(), text);
        msg.setParseMode("Markdown");
        return msg;
    }

    public static SendMessage start(Long chatId) {
        return simple(chatId, """
                👋 *Benvenuto nel Calendario Economico Bot!*

                ✅ Tutto è pronto.
                Usa /help per vedere i comandi disponibili.
                """);
    }

    public static SendMessage help(Long chatId) {
        return simple(chatId, """
                📌 *Comandi disponibili:*

                /oggi - Eventi economici previsti per *oggi*  
                /usa - Eventi in *dollari* (USD)  
                /eur - Eventi in *euro* (EUR)  
                /top - Eventi ad *alto impatto* ⭐⭐⭐  
                /lotto - Calcolo lotti consigliati  
                /screenshot - Screenshot grafico M15  
                /help - Questo elenco
                """);
    }

    public static SendMessage lottoHelp(Long chatId) {
        return simple(chatId, """
                🧮 *Calcolatore Lotto*

                ✏️ Formato comando:
                `/lotto <pair> <capitale> <rischio%> <stoploss pip>`

                📌 Esempio:
                `/lotto EURUSD 2000 1.5 15`

                👉 Significato:
                - *pair*: strumento (es: EURUSD, XAUUSD, US500, ecc.)
                - *capitale*: capitale in EUR
                - *rischio%*: rischio per trade
                - *SL pip*: distanza dello stop loss
                """);
    }

    public static SendMessage lottoResult(Long chatId, String[] parts, LottoCalculatorService service) {
        if (parts.length != 5) {
            return simple(chatId, "❗ Formato comando errato. Usa: /lotto EURUSD 2000 1.5 15");
        }

        try {
            String pair = parts[1];
            double capitale = Double.parseDouble(parts[2]);
            double rischio = Double.parseDouble(parts[3]);
            double sl = Double.parseDouble(parts[4]);
            String result = service.calcolaLotti(pair, capitale, rischio, sl);
            return simple(chatId, result);
        } catch (NumberFormatException e) {
            return simple(chatId, "⚠️ Parametri non validi. Esempio corretto: /lotto EURUSD 2000 1.5 15");
        }
    }

    public static SendPhoto screenshot(Long chatId, String pair, ScreenshotService service) {
        try {
            String url = service.getScreenshotUrlForPair(pair);
            if (url == null) {
                SendPhoto error = new SendPhoto();
                error.setChatId(chatId.toString());
                error.setCaption("❌ Pair non supportato: " + pair);
                return error;
            }

            SendPhoto photo = new SendPhoto();
            photo.setChatId(chatId.toString());
            photo.setPhoto(new InputFile(url));
            return photo;
        } catch (Exception e) {
            SendPhoto photo = new SendPhoto();
            photo.setChatId(chatId.toString());
            photo.setCaption("❌ Errore durante il recupero dello screenshot per " + pair);
            return photo;
        }
    }
}
