package com.ness.telegram.todolistbot;

import com.ness.telegram.todolistbot.bot.TodoListBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@SpringBootApplication
@Slf4j
public class Application {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TodoListBot bot =
                SpringApplication.run(Application.class, args)
                        .getBean(TodoListBot.class);
        try {
            new TelegramBotsApi().registerBot(bot);
        } catch (Exception e) {
            log.error("Error registering bot: {}", e.getMessage());
        }
    }
}