package com.widebot.economiccalendarbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class ScreenshotService {

    @Value("${SCREENSHOT_API_KEY}")
    private String apiKey;

    public String getScreenshotUrlForPair(String pair) {
        try {
            String url = "https://www.tradingview.com/chart/?symbol=FX:" + pair.toUpperCase();

            return "https://api.apiflash.com/v1/urltoimage?" +
                    "access_key=" + apiKey +
                    "&url=" + URLEncoder.encode(url, StandardCharsets.UTF_8) +
                    "&width=1280&height=720&format=jpeg";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
