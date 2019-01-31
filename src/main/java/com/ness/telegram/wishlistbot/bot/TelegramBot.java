package com.ness.telegram.wishlistbot.bot;

import java.util.List;
import java.util.Optional;
import com.ness.telegram.wishlistbot.model.State;
import com.ness.telegram.wishlistbot.model.User;
import com.ness.telegram.wishlistbot.model.Wish;
import com.ness.telegram.wishlistbot.service.UserService;
import com.ness.telegram.wishlistbot.service.WishAddCacheService;
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

    @Autowired
    private WishAddCacheService cacheService;

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String text = message.getText();
        Long chatId = message.getChatId();

        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        response.setParseMode("Markdown");

        User user = registerAndOrGet(chatId);

        if (text.equals(Command.CANCEL.getText())) {
            if (user.getState().equals(State.DEFAULT))
                response.setText("Nothing to cancel");
            else {
                user.setState(State.DEFAULT);
                userService.save(user);
                response.setText("Action canceled");
            }
            sendResponse(response);
            return;
        }

        switch (user.getState()) {
            case DEFAULT:
                response.setText(resolveDefaultState(text, user));
                break;

            case ADD_SETLABEL:
                user.setState(State.ADD_SETLINK);
                userService.save(user);
                cacheService.putLabel(chatId, text);
                response.setText("Enter wish link (/skip to leave empty)");
                break;

            case ADD_SETLINK:
                user.setState(State.ADD_SETPRICE);
                userService.save(user);
                cacheService.putLink(chatId, text);
                response.setText("Enter wish price (/skip to leave empty)");
                break;

            case ADD_SETPRICE:
                user.setState(State.DEFAULT);
                userService.save(user);

                String wishLabel = cacheService.getLabel(chatId);
                String wishLink = cacheService.getLink(chatId);
                String wishPrice = text;

                if (wishLabel == null || wishLink == null) {
                    response.setText("Whoops! Look's like I was waiting too long. Please try again.");
                } else {
                    Wish wish = new Wish();
                    wish.setUser(user);
                    wish.setLabel(wishLabel);

                    if (!wishLink.equals(Command.SKIP.getText()))
                        wish.setLink(wishLink);

                    if (!wishPrice.equals(Command.SKIP.getText()))
                        wish.setPrice(wishPrice);

                    wishService.save(wish);
                    response.setText("Wish " + wishLabel + " added");
                }
                break;

            case DELETE_CHOOSE:

                break;
        }

        sendResponse(response);
    }

    private String resolveDefaultState(String text, User user) {
        Command command = Command.ofText(text);
        Long chatId = user.getChatId();

        String result = "";

        switch (command) {
            case LIST:
                result = getWishesString(chatId);
                break;

            case ADD:
                result = "Enter wish text";
                user.setState(State.ADD_SETLABEL);
                userService.save(user);
                break;

            case REMOVE:
                result = "Enter number of wish(es) to remove (like '1' or '2 4 1')";
                user.setState(State.DELETE_CHOOSE);
                userService.save(user);
                break;

            case HELP:
                result = getHelpString();
                break;

            default:
                result = "Wrong command. See /help";
                break;
        }
        return result;
    }

    private String getWishesString(Long chatId) {
        List<Wish> wishes = wishService.findByUserChatId(chatId);
        if (wishes.isEmpty())
            return "You have no wishes yet";

        StringBuilder sb = new StringBuilder();
        wishes.sort(((w1, w2) -> w1.getId().compareTo(w2.getId())));
        Integer i = 1;

        for (Wish wish : wishes) {
            String label = wish.getLabel();
            String link = wish.getLink();
            String price = wish.getPrice();

            sb.append(i);
            sb.append(". ");

            if (!StringUtils.isEmpty(link)) {
                sb.append("[");
                sb.append(label);
                sb.append("](");
                sb.append(link);
                sb.append(")");
            } else {
                sb.append("*");
                sb.append(label);
                sb.append("*");
            }
            sb.append(" ");

            if (!StringUtils.isEmpty(price))
                sb.append(price);

            sb.append("\n");
            i++;
        }
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
        return "Here is the list of available commands:\n" + "/list - show your list of wishes\n"
                + "/add - add a new wish to your list\n"
                + "/remove - remove a wish from your wishlist\n"
                + "/cancel - break current action (while adding or removing only)\n"
                + "/help - show your list available commands\n";
    }
}
