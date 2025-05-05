package com.widebot.economiccalendarbot.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LottoCalculatorService {

    // Mappa dei valori pip per 1 lotto, per ogni strumento
    private static final Map<String, Double> pipValues;

    static {
        pipValues = new HashMap<>();
        pipValues.put("EURUSD", 10.0);     // classico FX
        pipValues.put("GBPUSD", 10.0);
        pipValues.put("XAUUSD", 1.0);      // oro
        pipValues.put("BTCUSD", 1.0);      // stimato
        pipValues.put("US500", 0.5);       // S&P 500
        pipValues.put("US100", 0.2);       // Nasdaq
        pipValues.put("GER40", 1.0);       // DAX (in EUR)
    }

    /**
     * Calcola i lotti consigliati per un trade.
     *
     * @param pair           es: "EURUSD"
     * @param capitale       capitale disponibile (es: 2000)
     * @param rischioPercent percentuale di rischio (es: 1.5)
     * @param stopLossPip   SL in pip/point (es: 15)
     * @return testo formattato con i risultati
     */
    public String calcolaLotti(String pair, double capitale, double rischioPercent, double stopLossPip) {
        pair = pair.toUpperCase();
        if (!pipValues.containsKey(pair)) {
            return "❌ Pair non supportato: " + pair;
        }

        double valorePip = pipValues.get(pair);
        double rischioEuro = capitale * (rischioPercent / 100.0);
        double lotti = rischioEuro / (stopLossPip * valorePip);
        lotti = Math.round(lotti * 100.0) / 100.0; // arrotonda a 2 decimali

        return String.format("""
                📈 *Calcolo Lotto per %s*
                Capitale: €%.2f
                Rischio: %.2f%% → €%.2f
                Stop Loss: %.1f pip
                Valore pip stimato: €%.2f
                📌 *Lotti consigliati*: *%.2f*
                """, pair, capitale, rischioPercent, rischioEuro, stopLossPip, valorePip, lotti);
    }
}
