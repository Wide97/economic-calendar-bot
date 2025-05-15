package com.widebot.economiccalendarbot.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DotenvLoader {

    @PostConstruct
    public void loadEnv() {
//        Dotenv dotenv = Dotenv.configure()
//                .ignoreIfMalformed()
//                .ignoreIfMissing()
//                .load();
        Dotenv dotenv = Dotenv.configure()
                .directory("/root/config")
                .filename(".env")
                .load();


        System.setProperty("bot.token", dotenv.get("bot.token"));
        System.setProperty("bot.name", dotenv.get("bot.name"));
        System.setProperty("bot.webhookPath", dotenv.get("bot.webhookPath"));
        System.setProperty("bot.webhookUrl", dotenv.get("bot.webhookUrl"));
        System.setProperty("twelvedata.apikey", dotenv.get("twelvedata.apikey"));
        System.setProperty("screenshot.api.key", dotenv.get("screenshot.api.key"));
    }
}
