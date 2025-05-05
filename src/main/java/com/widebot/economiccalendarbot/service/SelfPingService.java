package com.widebot.economiccalendarbot.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class SelfPingService {

    // Ping ogni 10 minuti (secondo = 0, minuto = ogni 10)
    @Scheduled(cron = "0 */10 * * * *")
    public void keepAlivePing() {
        try {
            URL url = new URL("https://economic-calendar-bot.onrender.com/ping"); // ⚠️ metti il tuo dominio corretto!
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int status = conn.getResponseCode();
            System.out.println("🔁 Ping automatico → HTTP " + status);
        } catch (Exception e) {
            System.out.println("❌ Errore nel ping automatico:");
            e.printStackTrace();
        }
    }
}
