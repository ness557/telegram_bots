package com.ness.telegram.wishlistbot.bot;

import com.ness.telegram.wishlistbot.model.State;
import com.ness.telegram.wishlistbot.model.User;
import com.ness.telegram.wishlistbot.model.Wish;
import com.ness.telegram.wishlistbot.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UpdateResolverImpl implements UpdateResolver {

    private static final String EMPTY_CACHE =
            "Whoops! Look's like I was waiting too long. Please try again.";

    @Autowired
    private UserService userService;

    @Autowired
    private WishService wishService;

    @Autowired
    @Qualifier("wishAddCacheServiceImpl")
    private WishAddCacheService cacheAddService;

    @Autowired
    private WishDeleteCacheService cacheDeleteService;

    @Autowired
    private WishEditCacheService cacheEditService;

    @Override
    public SendMessage resolve(Update update) {
        Message message = update.getMessage();
        String text = message.getText();
        Long chatId = message.getChatId();

        SendMessage response = new SendMessage();
        response.setChatId(String.valueOf(chatId));

        User user = registerAndOrGet(chatId);

        Command command = Command.ofText(text);

        switch (command) {
            case LIST:
                response.setText(getWishesString(chatId));
                response.disableWebPagePreview();
                user.setState(State.DEFAULT);
                userService.save(user);
                response.setParseMode("Markdown");
                break;

            case ADD:
                response.setText("Enter wish text");
                user.setState(State.ADD_SETLABEL);
                userService.save(user);
                break;

            case EDIT:
                response.setText("Enter number of wish to edit");
                user.setState(State.EDIT_CHOOSE);
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
                response.setText("Wrong command. See " + Command.HELP.getText());
                if (!user.getState().equals(State.DEFAULT))
                    resolveUserState(text, user, response);
                break;
        }

        return response;
    }

    private User registerAndOrGet(Long chatId) {
        Optional<User> user = userService.findByChatId(chatId);

        if (!user.isPresent()) {
            User newUser = new User();
            newUser.setChatId(chatId);
            userService.save(newUser);
            log.debug("New user registered with chatId [{}]", chatId);

            return newUser;
        }

        return user.get();
    }

    private void resolveUserState(String text, User user, SendMessage response) {
        Long chatId = user.getChatId();
        String responseString = "";

        switch (user.getState()) {

            case ADD_SETLABEL:
                user.setState(State.ADD_SETLINK);
                userService.save(user);

                // save label to cache
                cacheAddService.putLabel(chatId, text);
                responseString = "Enter wish link (" + Command.SKIP.getText() + " to leave empty)";
                break;

            case ADD_SETLINK:
                user.setState(State.ADD_SETPRICE);
                userService.save(user);

                // save link
                cacheAddService.putLink(chatId, text);
                responseString = "Enter wish price (" + Command.SKIP.getText() + " to leave empty)";
                break;

            case ADD_SETPRICE:
                user.setState(State.DEFAULT);
                userService.save(user);

                // get link and label from cache
                String wishLabel = cacheAddService.getLabel(chatId);
                String wishLink = cacheAddService.getLink(chatId);
                String wishPrice = text;

                responseString = addWish(wishLabel, wishLink, wishPrice, user);
                break;

            case DELETE_CHOOSE:
                responseString = chooseToDelete(text, user, response);
                break;

            case DELETE_CONFIRM:
                responseString = confirmDelete(text, user, response);
                break;

            case EDIT_CHOOSE:

                responseString = chooseToEdit(text, user);
                break;

            case EDIT_SETLABEL:
                responseString = editLabel(text, user);
                break;

            case EDIT_SETLINK:
                responseString = editLink(text, user);
                break;

            case EDIT_SETPRICE:
                responseString = editPrice(text, user);
                break;
        }
        response.setText(responseString);
    }

    private String addWish(String wishLabel, String wishLink, String wishPrice, User user) {
        if (wishLabel == null || wishLink == null)
            return EMPTY_CACHE;

        Wish wish = new Wish();
        wish.setUser(user);
        wish.setLabel(wishLabel);

        // user entered /skip command
        if (!wishLink.equals(Command.SKIP.getText()))
            wish.setLink(wishLink);

        // user entered /skip command
        if (!wishPrice.equals(Command.SKIP.getText()))
            wish.setPrice(wishPrice);

        wishService.save(wish);

        return "Wish " + wishLabel + " added";
    }

    private String getWishesString(Long chatId) {
        List<Wish> wishes = wishService.findByUserChatId(chatId);
        if (wishes.isEmpty())
            return "You have no wishes yet";

        StringBuilder sb = new StringBuilder();
        wishes.sort((Comparator.comparing(Wish::getId)));
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

    private String editLabel(String text, User user) {
        Long chatId = user.getChatId();
        Wish wish = cacheEditService.getWish(chatId);

        if (wish == null) {
            user.setState(State.DEFAULT);
            userService.save(user);
            return EMPTY_CACHE;
        }

        cacheEditService.putLabel(chatId, text);
        user.setState(State.EDIT_SETLINK);
        userService.save(user);

        String oldValue = wish.getLink();
        if (oldValue == null)
            oldValue = "Empty";

        return "Enter new wish link.\nCurrent link: " + oldValue + "\n" + Command.SKIP.getText()
                + " to leave current link";
    }

    private String editLink(String text, User user) {
        Long chatId = user.getChatId();
        Wish wish = cacheEditService.getWish(chatId);

        if (wish == null) {
            user.setState(State.DEFAULT);
            userService.save(user);
            return EMPTY_CACHE;
        }

        cacheEditService.putLink(chatId, text);
        user.setState(State.EDIT_SETPRICE);
        userService.save(user);

        String oldValue = wish.getPrice();
        if (oldValue == null)
            oldValue = "Empty";

        return "Enter new wish price.\nCurrent text: " + oldValue + "\n" + Command.SKIP.getText()
                + " to leave current price";
    }

    private String editPrice(String text, User user) {
        Long chatId = user.getChatId();
        Wish wish = cacheEditService.getWish(chatId);
        String newLabel = cacheEditService.getLabel(chatId);
        String newLink = cacheEditService.getLink(chatId);
        String newPrice = text;

        user.setState(State.DEFAULT);
        userService.save(user);

        if (wish == null || newLabel == null || newLink == null || newPrice == null)
            return EMPTY_CACHE;

        if (!newLabel.equals(Command.SKIP.getText()))
            wish.setLabel(newLabel);

        if (!newLink.equals(Command.SKIP.getText()))
            wish.setLink(newLink);

        if (!newPrice.equals(Command.SKIP.getText()))
            wish.setPrice(newPrice);

        wishService.save(wish);

        return "Ok. The new wish is:\n" + "Name: " + wish.getLabel() + "\n" + "Link: "
                + wish.getLink() + "\n" + "Price: " + wish.getPrice() + "\n";
    }

    private String chooseToDelete(String text, User user, SendMessage response) {
        Long chatId = user.getChatId();
        String error_delete = "Please specify existing wish number(s) in format '1' or '1 2 3'\n"
                + "use " + Command.CANCEL.getText() + " to abort deleting";
        try {

            // get list of wish indexes choosed to delete by user
            List<Integer> items = new ArrayList<>();
            for (String str : text.split(" ")) {
                items.add(NumberUtils.parseNumber(str, Integer.class));
            }

            // got empty string (somehow)
            if (items.isEmpty())
                return error_delete;

            // get all user's wishes
            List<Wish> wishes = wishService.findByUserChatId(chatId);
            // and sorting them by id
            wishes.sort(Comparator.comparing(Wish::getId));
            // get list of wishes to delete
            List<Wish> wishesToDelete = new ArrayList<>();
            for (Integer itemNumber : items)
                wishesToDelete.add(wishes.get(itemNumber - 1));

            // save their ids to cache
            cacheDeleteService.putWishes(chatId,
                    wishesToDelete.stream().map(w -> w.getId()).collect(Collectors.toList()));

            user.setState(State.DELETE_CONFIRM);
            userService.save(user);

            StringBuilder sb = new StringBuilder();
            sb.append("Are you sure you want to delete theese wishes:\n");
            wishesToDelete.stream().forEach(w -> sb.append(w.getLabel()).append("\n"));

            response.setReplyMarkup(getConfirmKeyboard());

            return sb.toString();
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {

            return error_delete;
        }
    }

    private String confirmDelete(String text, User user, SendMessage response) {
        Long chatId = user.getChatId();
        user.setState(State.DEFAULT);
        userService.save(user);

        response.setReplyMarkup(new ReplyKeyboardRemove());

        List<Long> wishes = cacheDeleteService.getWishes(chatId);
        if (wishes == null)
            return EMPTY_CACHE;

        switch (text) {
            case "Yes":
                // get all user's wishes and delete
                wishes.forEach(w -> wishService.delete(w));
                return "Ok. All is done. You can check it with " + Command.LIST.getText()
                        + " command";

            case "No":
                return "Ok. I did nothing";

            default:
                return "Sorry, can't recognise";
        }
    }

    private String chooseToEdit(String text, User user) {
        Long chatId = user.getChatId();
        String error_edit = "Please specify existing wish nubmer (like '1' or '3')\n" + "use "
                + Command.CANCEL.getText() + " to abort editing";

        try {
            Integer wishNumber = NumberUtils.parseNumber(text, Integer.class);
            List<Wish> wishes = wishService.findByUserChatId(chatId);
            wishes.sort(Comparator.comparing(Wish::getId));
            Wish wish = wishes.get(wishNumber - 1);
            cacheEditService.putWish(chatId, wish);
            user.setState(State.EDIT_SETLABEL);
            userService.save(user);

            return "Enter new wish text.\nCurrent text: " + wish.getLabel() + "\n"
                    + Command.SKIP.getText() + " to leave current text";

        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            return error_edit;
        }
    }

    private String getHelpString() {
        return "Here is the list of available commands:\n"
                + Command.LIST.getText() + " - show your list of wishes\n"
                + Command.ADD.getText() + " - add a new wish to your list\n"
                + Command.EDIT.getText() + " - edit a wish\n"
                + Command.REMOVE.getText() + " - remove a wish from your wishlist\n"
                + Command.CANCEL.getText() + " - break current action (while adding, editing or removing)\n"
                + Command.HELP.getText() + " - show your list available commands\n";
    }

    private ReplyKeyboardMarkup getConfirmKeyboard() {
        // confirm keyboard
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setOneTimeKeyboard(true);
        keyboard.setResizeKeyboard(true);
        keyboard.setSelective(true);

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton("Yes"));
        keyboardRow.add(new KeyboardButton("No"));
        keyboard.setKeyboard(Arrays.asList(keyboardRow));

        return keyboard;
    }
}
