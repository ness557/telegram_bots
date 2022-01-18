package com.ness.telegram.wishlistbot;

import com.ness.telegram.wishlistbot.bot.TelegramBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
@Slf4j
public class Application {
	public static void main(String[] args) {
		TelegramBot bot = SpringApplication.run(Application.class, args).getBean(TelegramBot.class);
		try {
			new TelegramBotsApi(DefaultBotSession.class).registerBot(bot);
		} catch (TelegramApiException e) {
			log.error("Error registering bot: {}", e.getMessage());
		}
	}
}