package com.widebot.economiccalendarbot.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExchangeRateService {

    private final OkHttpClient client = new OkHttpClient();

    @Value("${twelvedata.apikey}")
    private String apiKey;

    public double getEurUsdRate() throws Exception {
        Request request = new Request.Builder()
                .url("https://api.twelvedata.com/price?symbol=EUR/USD&apikey=" + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new RuntimeException("Errore HTTP: " + response.code());
            JSONObject json = new JSONObject(response.body().string());
            return json.getDouble("price");
        }
    }
}
