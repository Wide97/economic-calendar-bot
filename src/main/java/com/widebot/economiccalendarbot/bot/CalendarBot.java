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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
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
                    salvaChatIdSeNuovo(chatId);
                    sendMessage(chatId, """
        👋 Benvenuto nel Calendario Economico Bot 📊

        Questo bot ti permette di ricevere, direttamente qui su Telegram, gli eventi economici rilevanti ogni giorno.

        ✏️ *Comandi disponibili:*
        • /oggi → Tutti gli eventi economici di oggi
        • /usa → Solo eventi in USD (USA)
        • /eur → Solo eventi in EUR (Eurozona)
        • /top → Eventi ad alto impatto (⭐⭐⭐)
        • /help → Rivedi questo elenco

        📩 *Consiglio:* Aggiungi questo bot ai tuoi preferiti o fissalo in alto per consultarlo ogni mattina prima di tradare!
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

                case "/top":
                    String topEventi = economicEventService.getEventiAdAltoImpatto();
                    sendMessage(chatId, topEventi);
                    break;

                case "/help":
                    sendMessage(chatId, """
                    📌 Comandi disponibili:

                    /start - Messaggio di benvenuto
                    /oggi - Eventi economici previsti per oggi
                    /usa - Eventi USA (USD)
                    /eur - Eventi EURO (EUR)
                    /top - Eventi ad alto impatto ⭐⭐⭐
                    /help - Questo elenco
                    """);
                    break;

                case "/testnotifica":
                    inviaEventiAdAltoImpattoATutti(); // chiama il metodo come se fosse schedulato
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
        message.setParseMode("Markdown");
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

    //@Scheduled(cron = "0 30 8 * * *")
    public void inviaEventiAdAltoImpattoATutti() {
        String messaggio = economicEventService.getEventiAdAltoImpatto();
        List<Long> chatIds = getChatIdsFromFile();
        for (Long chatId : chatIds) {
            sendMessage(chatId, messaggio);
            System.out.println("✅ Inviato a: " + chatId);
        }
    }


    private List<Long> getChatIdsFromFile() {
        try {
            Path path = Paths.get("src/main/resources/chat_ids.txt");
            if (!Files.exists(path)) return new ArrayList<>();
            return Files.readAllLines(path).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .distinct()
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void salvaChatIdSeNuovo(Long chatId) {
        List<Long> esistenti = getChatIdsFromFile();
        if (!esistenti.contains(chatId)) {
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter("src/main/resources/chat_ids.txt", true))) {
                writer.write(chatId.toString());
                writer.newLine();
                System.out.println("💾 Salvato nuovo chatId: " + chatId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




}

