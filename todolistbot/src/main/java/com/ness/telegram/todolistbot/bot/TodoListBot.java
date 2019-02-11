package com.ness.telegram.todolistbot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Component
@Slf4j
public class TodoListBot extends TelegramLongPollingBot {

    @Value("${telegram.bot_name}")
    private String name;

    @Value("${telegram.api_key}")
    private String token;

    @Autowired
    private UpdateResolver resolver;

    @Override
    public void onUpdateReceived(Update update) {
        BotApiMethod response = resolver.resolve(update);
        try {
            execute(response);
        } catch (TelegramApiException e) {
            if(e instanceof TelegramApiRequestException){
                log.error("Couldn't send message: {}", ((TelegramApiRequestException) e).getApiResponse());
                return;
            }
            log.error("Couldn't send message: {}", e.getCause().getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }
}
