//package com.widebot.economiccalendarbot.service;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//public class ScreenshotService {
//
//    @Value("${screenshot.api.key}")
//    private String apiKey;
//
//    private static final int WIDTH = 1280;
//    private static final int HEIGHT = 720;
//    private static final String BASE_SCREENSHOT_URL = "https://shot.screenshotapi.net/screenshot";
//
//    private static final Map<String, String> SYMBOL_MAP = new HashMap<>();
//
//    static {
//        SYMBOL_MAP.put("EURUSD", "OANDA:EURUSD");
//        SYMBOL_MAP.put("GBPUSD", "FX:GBPUSD");
//        SYMBOL_MAP.put("XAUUSD", "OANDA:XAUUSD");
//        SYMBOL_MAP.put("BTCUSD", "BITSTAMP:BTCUSD");
//        SYMBOL_MAP.put("US500", "OANDA:SPX500USD");
//        SYMBOL_MAP.put("US100", "CURRENCYCOM:US100");
//        SYMBOL_MAP.put("GER40", "FPMARKETS:GER40");
//    }
//
//    /**
//     * Genera lo screenshot dinamico di un grafico TradingView con pair e timeframe scelti.
//     *
//     * @param pair Symbol dell’asset (es. EURUSD)
//     * @param timeframe Timeframe (es. "15", "60", "240", "1D")
//     * @return URL dinamico anti-cache oppure null se il pair non è valido
//     */
//    public String getScreenshotUrl(String pair, String timeframe) {
//        if (pair == null || timeframe == null) return null;
//
//        String symbol = SYMBOL_MAP.get(pair.toUpperCase());
//        if (symbol == null) return null;
//
//        long timestamp = System.currentTimeMillis();
//
//        // URL al widget embed di TradingView con simbolo e intervallo dinamici
//        String widgetUrl = "https://s.tradingview.com/widgetembed/?frameElementId=tradingview_xxx"
//                + "&symbol=" + URLEncoder.encode(symbol, StandardCharsets.UTF_8)
//                + "&interval=" + URLEncoder.encode(timeframe, StandardCharsets.UTF_8)
//                + "&hidesidetoolbar=1"
//                + "&hidetoptoolbar=1"
//                + "&hidelegend=1"
//                + "&theme=dark"
//                + "&style=1"
//                + "&timezone=Europe/Rome";
//
//        // Screenshot API con link dinamico + anti-cache
//        return BASE_SCREENSHOT_URL
//                + "?token=" + apiKey
//                + "&url=" + URLEncoder.encode(widgetUrl, StandardCharsets.UTF_8)
//                + "&output=image"
//                + "&file_type=png"
//                + "&wait_for_event=load"
//                + "&fresh=true"
//                + "&cache=false"
//                + "&width=" + WIDTH
//                + "&height=" + HEIGHT
//                + "&timestamp=" + timestamp;
//    }
//}
//
