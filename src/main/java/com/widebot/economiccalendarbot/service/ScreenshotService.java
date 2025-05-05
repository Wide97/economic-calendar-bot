package com.widebot.economiccalendarbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ScreenshotService {

    @Value("${screenshot.api.key}")
    private String apiKey;

    public String getScreenshotUrlForPair(String pair) {
        String tradingViewSymbol = switch (pair.toUpperCase()) {
            case "EURUSD" -> "FX:EURUSD";
            case "GBPUSD" -> "FX:GBPUSD";
            case "XAUUSD" -> "OANDA:XAUUSD";
            case "BTCUSD" -> "BITSTAMP:BTCUSD";
            case "US500" -> "OANDA:SPX500USD";
            case "US100" -> "FPMARKETS:USTECH";
            case "GER40" -> "FPMARKETS:GER40";
            default -> null;
        };

        if (tradingViewSymbol == null) return null;

        String chartUrl = "https://www.tradingview.com/chart/?symbol=" + tradingViewSymbol;
        return "https://shot.screenshotapi.net/screenshot"
                + "?token=" + apiKey
                + "&url=" + chartUrl
                + "&output=image&file_type=png&wait_for_event=load";
    }
}
