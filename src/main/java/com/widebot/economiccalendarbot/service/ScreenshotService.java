package com.widebot.economiccalendarbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class ScreenshotService {

    @Value("${screenshot.api.key}")
    private String apiKey;

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final String BASE_SCREENSHOT_URL = "https://shot.screenshotapi.net/screenshot";

    private static final Map<String, String> SYMBOL_MAP = new HashMap<>();

    static {
        SYMBOL_MAP.put("EURUSD", "OANDA:EURUSD");
        SYMBOL_MAP.put("GBPUSD", "FX:GBPUSD");
        SYMBOL_MAP.put("XAUUSD", "OANDA:XAUUSD");
        SYMBOL_MAP.put("BTCUSD", "BITSTAMP:BTCUSD");
        SYMBOL_MAP.put("US500", "OANDA:SPX500USD");
        SYMBOL_MAP.put("US100", "CURRENCYCOM:US100"); // oppure FPMARKETS:US100
        SYMBOL_MAP.put("GER40", "FPMARKETS:GER40");
    }

    /**
     * Restituisce l'URL completo per ottenere uno screenshot TradingView del pair specificato.
     *
     * @param pair Il simbolo dell'asset (es. EURUSD, US100, GER40)
     * @return URL dello screenshot oppure null se il pair non è gestito
     */
    public String getScreenshotUrlForPair(String pair) {
        if (pair == null || pair.trim().isEmpty()) return null;

        String tradingViewSymbol = SYMBOL_MAP.get(pair.toUpperCase());
        if (tradingViewSymbol == null) return null;

        String chartUrl = "https://www.tradingview.com/chart/?symbol=" +
                URLEncoder.encode(tradingViewSymbol, StandardCharsets.UTF_8);

        return BASE_SCREENSHOT_URL
                + "?token=" + apiKey
                + "&url=" + URLEncoder.encode(chartUrl, StandardCharsets.UTF_8)
                + "&output=image"
                + "&file_type=png"
                + "&wait_for_event=load"
                + "&width=" + WIDTH
                + "&height=" + HEIGHT;
    }
}
