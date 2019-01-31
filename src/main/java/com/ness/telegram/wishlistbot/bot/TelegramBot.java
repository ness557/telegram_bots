package com.ness.telegram.wishlistbot.bot;

import java.util.List;
import java.util.Optional;
import com.ness.telegram.wishlistbot.model.User;
import com.ness.telegram.wishlistbot.model.Wish;
import com.ness.telegram.wishlistbot.service.UserService;
import com.ness.telegram.wishlistbot.service.WishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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
        String text = message.getText();
        Long chatId = message.getChatId();

        User user = registerAndOrGet(chatId);
        switch (user.getState()) {
            case DEFAULT:
                resolveDefaultState(text, chatId);
                break;
        }
    }

    private void resolveDefaultState(String text, Long chatId) {
        Command command = Command.ofText(text);
        SendMessage response = new SendMessage();
        response.setChatId(chatId);

        switch (command) {
            case LIST:
                response.setText(getWishesString(chatId));
                break;

            case ADD:

                break;

            case REMOVE:

                break;

            case HELP:
                response.setText(getHelpString());
                break;

            default:
                response.setText("Wrong command. See /help");
                break;
        }
        sendResponse(response);
    }

    private String getWishesString(Long chatId) {
        List<Wish> wishes = wishService.findByUserChatId(chatId);
        StringBuilder sb = new StringBuilder();
        wishes.stream().forEach(wish -> {
            String label = wish.getLabel();
            String link = wish.getLink();
            String price = wish.getPrice();

            if (!StringUtils.isEmpty(link)) {
                sb.append("[");
                sb.append(label);
                sb.append("](");
                sb.append(link);
            } else {
                sb.append("*");
                sb.append(label);
                sb.append("*");
            }

            sb.append(" ");
            sb.append(price);
            sb.append("\n");
        });
        return sb.toString();
    }

    private User registerAndOrGet(Long chatId) {
        Optional<User> user = userService.findByChatId(chatId);

        if (!user.isPresent()) {
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

    private String getHelpString() {
        return "Here is the list of available commands:\n"
               + "/list - show your list of wishes\n"
               + "/add - add a new wish to your list\n"
               + "/remove - remove a wish from your wishlist\n"
               + "/abort - break current action (while adding or removing)"
               + "/help - show your list available commands\n";
    }
}