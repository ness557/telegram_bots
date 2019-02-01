package com.ness.telegram.wishlistbot.bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import com.ness.telegram.wishlistbot.model.State;
import com.ness.telegram.wishlistbot.model.User;
import com.ness.telegram.wishlistbot.model.Wish;
import com.ness.telegram.wishlistbot.service.UserService;
import com.ness.telegram.wishlistbot.service.WishAddCacheService;
import com.ness.telegram.wishlistbot.service.WishDeleteCacheService;
import com.ness.telegram.wishlistbot.service.WishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private static final String EMPTY_CACHE = "Whoops! Look's like I was waiting too long. Please try again.";

    @Value("${telegram.bot_name}")
    private String name;

    @Value("${telegram.api_key}")
    private String token;

    @Autowired
    private UserService userService;

    @Autowired
    private WishService wishService;

    @Autowired
    private WishAddCacheService cacheAddService;

    @Autowired
    private WishDeleteCacheService cacheDeleteService;

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String text = message.getText();
        Long chatId = message.getChatId();

        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        response.setParseMode("Markdown");

        User user = registerAndOrGet(chatId);

        Command command = Command.ofText(text);

        switch (command) {
            case LIST:
                response.setText(getWishesString(chatId));
                user.setState(State.DEFAULT);
                userService.save(user);
                break;

            case ADD:
                response.setText("Enter wish text");
                user.setState(State.ADD_SETLABEL);
                userService.save(user);
                break;

            case REMOVE:
                response.setText("Enter number of wish(es) to remove (like '1' or '2 4 1')");
                user.setState(State.DELETE_CHOOSE);
                userService.save(user);
                break;

            case HELP:
                response.setText(getHelpString());
                user.setState(State.DEFAULT);
                userService.save(user);
                break;

            case CANCEL:
                if (user.getState().equals(State.DEFAULT))
                    response.setText("Nothing to cancel");
                else {
                    user.setState(State.DEFAULT);
                    userService.save(user);
                    response.setText("Action canceled");
                }
                break;

            default:
                response.setText("Wrong command. See /help");
                if (!user.getState().equals(State.DEFAULT))
                    response.setText(resolveUserState(text, user));
                break;
        }
        sendResponse(response);
    }

    private String resolveUserState(String text, User user) {

        Long chatId = user.getChatId();
        String responseString = "";

        switch (user.getState()) {

            case ADD_SETLABEL:
                user.setState(State.ADD_SETLINK);
                userService.save(user);
                cacheAddService.putLabel(chatId, text);
                responseString = "Enter wish link (/skip to leave empty)";
                break;

            case ADD_SETLINK:
                user.setState(State.ADD_SETPRICE);
                userService.save(user);
                cacheAddService.putLink(chatId, text);
                responseString = "Enter wish price (/skip to leave empty)";
                break;

            case ADD_SETPRICE:
                user.setState(State.DEFAULT);
                userService.save(user);

                String wishLabel = cacheAddService.getLabel(chatId);
                String wishLink = cacheAddService.getLink(chatId);
                String wishPrice = text;

                if (wishLabel == null || wishLink == null) {
                    responseString = EMPTY_CACHE;

                } else {
                    Wish wish = new Wish();
                    wish.setUser(user);
                    wish.setLabel(wishLabel);

                    if (!wishLink.equals(Command.SKIP.getText()))
                        wish.setLink(wishLink);

                    if (!wishPrice.equals(Command.SKIP.getText()))
                        wish.setPrice(wishPrice);

                    wishService.save(wish);
                    responseString = "Wish " + wishLabel + " added";
                }
                break;

            case DELETE_CHOOSE:
                String error = "Please specify number(s) in format '1' or '1 2 3'\n"
                                + "use /cancel to abort deleting";
                try {
                    List<Integer> items = new ArrayList<>();
                    for (String str : text.split(" ")) {
                        items.add(NumberUtils.parseNumber(str, Integer.class));
                    }
                    if (items.isEmpty())
                        responseString = error;

                    List<Wish> wishes = wishService.findByUserChatId(chatId);
                    wishes.sort((w1, w2) -> w1.getId().compareTo(w2.getId()));
                    List<Wish> wishesToDelete = new ArrayList<>();

                    for (Integer itemNumber : items) {
                        wishesToDelete.add(wishes.get(itemNumber - 1));
                    }
                    cacheDeleteService.putWishes(chatId, wishesToDelete);
                    
                    user.setState(State.DELETE_CONFIRM);
                    userService.save(user);
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("Are you sure you want to delete theese wishes:\n");
                    wishesToDelete.stream().forEach(w -> sb.append(w.getLabel()).append("\n"));
                    responseString = sb.toString();
                } catch (IllegalArgumentException e) {
                    responseString = error;
                }
                break;
        }

        return responseString;
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
