package com.widebot.economiccalendarbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class ScreenshotService {

    @Value("${screenshot.api.key}")
    private String apiKey;

    /**
     * Restituisce l'URL completo per ottenere uno screenshot TradingView del pair specificato.
     * @param pair Il simbolo dell'asset, es. EURUSD, US100, GER40
     * @return URL dello screenshot oppure null se il pair non è gestito
     */
    public String getScreenshotUrlForPair(String pair) {
        // Mappa dei simboli TradingView per ciascun asset
        String tradingViewSymbol = switch (pair.toUpperCase()) {
            case "EURUSD" -> "FX:EURUSD";
            case "GBPUSD" -> "FX:GBPUSD";
            case "XAUUSD" -> "OANDA:XAUUSD";
            case "BTCUSD" -> "BITSTAMP:BTCUSD";
            case "US500" -> "OANDA:SPX500USD";
            case "US100" -> "FPMARKETS:USTECH"; // oppure FPMARKETS:US100 se preferisci
            case "GER40" -> "FPMARKETS:GER40";
            default -> null;
        };

        // Se il pair non è supportato
        if (tradingViewSymbol == null) return null;

        // Costruzione URL del grafico TradingView
        String chartUrl = "https://www.tradingview.com/chart/?symbol=" +
                URLEncoder.encode(tradingViewSymbol, StandardCharsets.UTF_8);

        // Costruzione URL finale dello screenshot (usando ScreenshotAPI.net)
        return "https://shot.screenshotapi.net/screenshot"
                + "?token=" + apiKey
                + "&url=" + URLEncoder.encode(chartUrl, StandardCharsets.UTF_8)
                + "&output=image"
                + "&file_type=png"
                + "&wait_for_event=load"
                + "&width=1280"
                + "&height=720";
    }
}
