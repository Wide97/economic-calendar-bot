package com.widebot.economiccalendarbot.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.springframework.beans.factory.annotation.Value;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class LottoCalculatorService {

    private final OkHttpClient client = new OkHttpClient();
    @Value("${twelvedata.apikey}")
    private String apiKey;

    public String calcolaLotti(String pair, double capitale, double rischio, double sl) {
        try {
            double eurUsd = getEurUsdExchangeRate();

            return switch (pair.toUpperCase()) {
                case "EURUSD" -> calcolaForex(pair, capitale, rischio, sl, eurUsd);
                 case "GBPUSD" -> calcolaForex(pair, capitale, rischio, sl, eurUsd);
                case "XAUUSD" -> calcolaXauUsd(capitale, rischio, sl, eurUsd);
                case "BTCUSD" -> calcolaBtcUsd(capitale, rischio, sl, eurUsd);
                case "US500", "US100", "GER40" -> calcolaIndice(pair, capitale, rischio, sl, eurUsd);
                default -> "❌ Pair non supportato: " + pair;
            };

        } catch (Exception e) {
            return "❌ Errore nel calcolo: " + e.getMessage();
        }
    }

    private double getEurUsdExchangeRate() throws Exception {
        Request request = new Request.Builder()
                .url("https://api.twelvedata.com/price?symbol=EUR/USD&apikey=" + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new RuntimeException("Errore HTTP");
            JSONObject json = new JSONObject(response.body().string());
            return json.getDouble("price");
        }
    }

    private String calcolaForex(String pair, double capitale, double rischio, double sl, double eurUsd) {
        double pipValueUsd = 10.0; // 1 pip = $10 per 1.00 lotto standard
        double rischioEur = capitale * rischio / 100;
        double rischioUsd = rischioEur * eurUsd;
        double lotto = rischioUsd / (pipValueUsd * sl);

        return format(pair, rischioEur, sl, lotto);
    }

    private String calcolaXauUsd(double capitale, double rischio, double sl, double eurUsd) {
        double pipValueUsd = 1.0; // 1 pip = $1 per 1.00 lotto
        double rischioEur = capitale * rischio / 100;
        double rischioUsd = rischioEur * eurUsd;
        double lotto = rischioUsd / (pipValueUsd * sl);

        return format("XAUUSD", rischioEur, sl, lotto);
    }

    private String calcolaBtcUsd(double capitale, double rischio, double sl, double eurUsd) {
        double rischioEur = capitale * rischio / 100;
        double rischioUsd = rischioEur * eurUsd;
        double lotto = rischioUsd / sl;

        return format("BTCUSD", rischioEur, sl, lotto);
    }

    private String calcolaIndice(String pair, double capitale, double rischio, double sl, double eurUsd) {
        double rischioEur = capitale * rischio / 100;
        double rischioUsd = rischioEur * eurUsd;
        double lotto = rischioUsd / sl; // 1 punto = 1$ per 1 lotto standard

        return format(pair, rischioEur, sl, lotto);
    }

    private String format(String pair, double rischioEur, double sl, double lotto) {
        return String.format("""
                📊 *Calcolo Lotto per %s*

                Capitale a rischio: €%.2f
                Stop loss: %.1f
                📈 Lotto consigliato: %.2f
                """, pair, rischioEur, sl, lotto);
    }
}
