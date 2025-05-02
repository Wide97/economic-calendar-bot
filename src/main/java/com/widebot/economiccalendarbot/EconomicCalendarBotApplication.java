package com.widebot.economiccalendarbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EconomicCalendarBotApplication {
	public static void main(String[] args) {
		SpringApplication.run(EconomicCalendarBotApplication.class, args);
	}
}
