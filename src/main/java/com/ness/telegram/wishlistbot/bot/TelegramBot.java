package com.ness.telegram.wishlistbot.bot;

import java.util.Optional;
import com.ness.telegram.wishlistbot.model.User;
import com.ness.telegram.wishlistbot.service.UserService;
import com.ness.telegram.wishlistbot.service.WishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot_name}")
    private String name;

    @Value("${telegram.api_key}")
    private String token;

    @Autowired
    private UserService userService;

    @Autowired
    private WishService wishService;

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();

        Long chatId = message.getChatId();

        User user = registerAndOrGet(chatId);




    }

    private User registerAndOrGet(Long chatId) {
        Optional<User> user = userService.findByChatId(chatId);
        
        if(!user.isPresent()){
            User newUser = new User();
            newUser.setChatId(chatId);
            userService.save(newUser);

            SendMessage response = new SendMessage(chatId, "You've been registered in system");
            sendResponse(response);

            return newUser;
        }

        return user.get();
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    private void sendResponse(SendMessage response) {
        try {
            execute(response);
        } catch (TelegramApiException e) {
            log.error("Error sending response: {}", e.getMessage());
        }
    } 
}