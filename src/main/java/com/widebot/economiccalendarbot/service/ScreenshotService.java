package com.widebot.economiccalendarbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ScreenshotService {

    @Value("${screenshot.api.key}")
    private String screenshotApiKey;

    /**
     * Costruisce un link diretto all'immagine PNG del grafico su TradingView per il pair dato.
     * Esempio: EURUSD → https://www.tradingview.com/chart/?symbol=FX:EURUSD
     */
    public String getScreenshotUrlForPair(String pair) {
        String tradingViewUrl = "https://www.tradingview.com/chart/?symbol=FX:" + pair.toUpperCase();
        return "https://api.screenshotapi.net/screenshot" +
                "?token=" + screenshotApiKey +
                "&url=" + tradingViewUrl +
                "&output=image&file_type=png&viewport=1920x1080&fresh=true";
    }
}
