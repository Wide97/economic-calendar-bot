package com.widebot.economiccalendarbot.bot;

import com.widebot.economiccalendarbot.service.EconomicEventService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.ZoneId;
import java.util.TimeZone;

@Component
public class CalendarBot extends TelegramLongPollingBot {

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.name}")
    private String botName;

    private final EconomicEventService economicEventService;

    public CalendarBot(EconomicEventService economicEventService) {
        this.economicEventService = economicEventService;
        System.out.println("✅ CalendarBot istanziato!");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String msg = update.getMessage().getText().trim();
            Long chatId = update.getMessage().getChatId();

            System.out.println("👉 Messaggio ricevuto: " + msg + " da chatId: " + chatId);

            switch (msg.toLowerCase()) {
                case "/start":
                    sendMessage(chatId, """
                    👋 Benvenuto nel Calendario Economico Bot!
                    Digita uno dei seguenti comandi:

                    /oggi - Eventi economici di oggi
                    /usa - Eventi solo degli Stati Uniti
                    /eur - Eventi solo dell'Eurozona
                    /help - Mostra l'elenco dei comandi
                    """);
                    break;

                case "/oggi":
                    String calendario = economicEventService.getCalendarioDiOggi();
                    sendMessage(chatId, calendario);
                    break;

                case "/usa":
                    String eventiUsa = economicEventService.getEventiPerValuta("USD");
                    sendMessage(chatId, eventiUsa);
                    break;

                case "/eur":
                    String eventiEur = economicEventService.getEventiPerValuta("EUR");
                    sendMessage(chatId, eventiEur);
                    break;

                case "/help":
                    sendMessage(chatId, """
                    📌 Comandi disponibili:

                    /start - Messaggio di benvenuto
                    /oggi - Eventi economici previsti per oggi
                    /usa - Eventi USA (USD)
                    /eur - Eventi EURO (EUR)
                    /help - Questo elenco
                    """);
                    break;

                default:
                    sendMessage(chatId, "❌ Comando non riconosciuto. Scrivi /help per vedere i comandi disponibili.");
                    break;
            }
        }
    }


    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @PostConstruct
    public void init() {
        // Imposta il timezone italiano
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Europe/Rome")));
    }

    @Scheduled(cron = "0 0 8 * * *") // orario italiano grazie al TimeZone sopra
    public void invioGiornaliero() {
        Long chatId = 461782101L;
        String risposta = economicEventService.getCalendarioDiOggi();
        sendMessage(chatId, risposta);
    }

}

