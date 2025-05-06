package com.widebot.economiccalendarbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;

@Service
public class LottoCalculatorService {

    private final ExchangeRateService exchangeRateService;

    private static final double FOREX_PIP_VALUE = 10.0;
    private static final double XAU_PIP_VALUE = 1.0;

    private static final DecimalFormat df = new DecimalFormat("#.##");

    @Autowired
    public LottoCalculatorService(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    public String calcolaLotti(String pair, double capitale, double rischio, double sl) {
        try {
            if (sl <= 0) return "❌ Lo stop loss deve essere maggiore di zero.";
            if (capitale <= 0 || rischio <= 0) return "❌ Capitale e rischio devono essere maggiori di zero.";

            double eurUsd = exchangeRateService.getEurUsdRate();

            return switch (pair.toUpperCase()) {
                case "EURUSD", "GBPUSD" -> calcolaForex(pair, capitale, rischio, sl, eurUsd);
                case "XAUUSD" -> calcolaXauUsd(capitale, rischio, sl, eurUsd);
                case "BTCUSD" -> calcolaBtcUsd(capitale, rischio, sl, eurUsd);
                case "US500", "US100", "GER40" -> calcolaIndice(pair, capitale, rischio, sl, eurUsd);
                default -> "❌ Pair non supportato: " + pair;
            };

        } catch (Exception e) {
            return "❌ Errore nel calcolo: " + e.getMessage();
        }
    }

    private String calcolaForex(String pair, double capitale, double rischio, double sl, double eurUsd) {
        double rischioEur = capitale * rischio / 100;
        double rischioUsd = rischioEur * eurUsd;
        double lotto = rischioUsd / (FOREX_PIP_VALUE * sl);

        return format(pair, capitale, rischio, rischioEur, sl, lotto);
    }

    private String calcolaXauUsd(double capitale, double rischio, double sl, double eurUsd) {
        double rischioEur = capitale * rischio / 100;
        double rischioUsd = rischioEur * eurUsd;
        double lotto = rischioUsd / (XAU_PIP_VALUE * sl);

        return format("XAUUSD", capitale, rischio, rischioEur, sl, lotto);
    }

    private String calcolaBtcUsd(double capitale, double rischio, double sl, double eurUsd) {
        double rischioEur = capitale * rischio / 100;
        double rischioUsd = rischioEur * eurUsd;
        double lotto = rischioUsd / sl;

        return format("BTCUSD", capitale, rischio, rischioEur, sl, lotto);
    }

    private String calcolaIndice(String pair, double capitale, double rischio, double sl, double eurUsd) {
        double rischioEur = capitale * rischio / 100;
        double rischioUsd = rischioEur * eurUsd;
        double lotto = rischioUsd / sl; // 1 punto = 1$ per 1 lotto standard

        return format(pair, capitale, rischio, rischioEur, sl, lotto);
    }

    private String format(String pair, double capitale, double rischioPercent, double rischioEur, double sl, double lotto) {
        return String.format("""
                📊 *Calcolo Lotto per %s*

                Capitale totale: €%s
                Rischio: %s%%
                Capitale a rischio: €%s
                Stop loss: %.1f
                📈 Lotto consigliato: %s
                """,
                pair,
                df.format(capitale),
                df.format(rischioPercent),
                df.format(rischioEur),
                sl,
                df.format(lotto)
        );
    }
}
