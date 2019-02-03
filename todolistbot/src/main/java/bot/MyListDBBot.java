package main.java.bot;


import main.java.model.dao.INoteDAO;
import main.java.model.dao.INoteListDAO;
import main.java.model.dao.IUserDAO;
import main.java.model.entity.Note;
import main.java.model.entity.NoteList;
import main.java.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class MyListDBBot extends TelegramLongPollingBot {

    private IUserDAO userDAO;
    private INoteListDAO noteListDAO;
    private INoteDAO noteDAO;
    private Environment env;

    private static final String PLEASE_CHOOSE_LIST = "Please choose list at first";

    private static final int NOTHING = 0;
    private static final int ADDLIST = 1;
    private static final int CHOOSELIST = 2;
    private static final int ADDNOTE = 3;
    private static final int SETDONE = 4;
    private static final int REMOVELIST = 5;
    private static final int REMOVENOTE = 6;


    private static final String[] requestCommands = {
            "/help", //0
            "/addList", //1
            "/addNote", //2
            "/getLists", //3
            "/chooseList", //4
            "/getNotes", //5
            "/setDone", //6
            "/removeList", //7
            "/removeNote", //8
            "/menu", //9
            "/cancel" //10

    };

    @Autowired
    public MyListDBBot(IUserDAO userDAO, INoteListDAO noteListDAO, INoteDAO noteDAO, Environment env) {
        this.noteDAO = noteDAO;
        this.noteListDAO = noteListDAO;
        this.userDAO = userDAO;
        this.env = env;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            // Set variables
            long chat_id = update.getMessage().getChatId();
            User user = userDAO.getByChatId(chat_id);
            String message_text = update.getMessage().getText();
            // user sent message
            // prepare reply message
            SendMessage message = new SendMessage()
                    .setChatId(chat_id);


            // user unregistered
            if (user == null)
                onRegister(chat_id, message);

                // user has saved statement
            else if (user.getCurrentStatement() > NOTHING)
                handleWaitForInputMessage(user, message_text, message);

                // looks like a simple text
            else
                onSendMenu(message);

            try {
                // Sending our message to user
                execute(message);
                // catch telegram api exception
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            // user pushed a button
        } else if (update.hasCallbackQuery()) {

            // Set variables
            long chat_id = update.getCallbackQuery().getMessage().getChatId();
            User user = userDAO.getByChatId(chat_id);

            //create reply message
            EditMessageText message = new EditMessageText()
                    .setChatId(chat_id)
                    .setMessageId(update.getCallbackQuery().getMessage().getMessageId());

            //handle message
            // user has saved statement
            if (user.getCurrentStatement() > NOTHING)
                handleWaitForInput(user, update.getCallbackQuery().getData(), message);

            else
                handleMessage(user, update.getCallbackQuery().getData(), message);

            try {
                // Sending our message object to user
                execute(message);
                // catch telegram api exception
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

    }

    // input handler for buttons
    private void handleWaitForInput(User user, String text, EditMessageText replyMessage) {

        // user pressed cancel button
        if (text.equals(requestCommands[10])) {
            onCancel(replyMessage, user);
            return;
        }

        // check user statement
        switch (user.getCurrentStatement()) {

            // user is choosing a list
            case CHOOSELIST:

                replyMessage.setText(handleChooseList(user, text));
                replyMessage.setReplyMarkup(getMenuKeyboard());
                break;

            // user is setting a note to done
            case SETDONE:

                replyMessage.setText(handleSetDone(user, text));
                replyMessage.setReplyMarkup(getMenuKeyboard());
                break;

            // user is removing list
            case REMOVELIST:

                replyMessage.setText(handleRemoveList(user, text));
                replyMessage.setReplyMarkup(getMenuKeyboard());
                break;

            // user is removing note
            case REMOVENOTE:

                replyMessage.setText(handleRemoveNote(user, text));
                replyMessage.setReplyMarkup(getMenuKeyboard());
                break;
        }
    }

    // input handler for text
    private void handleWaitForInputMessage(User user, String text, SendMessage replyMessage) {

        // check user statement
        switch (user.getCurrentStatement()) {

            // user is adding a new list
            case ADDLIST:

                replyMessage.setText(handleAddList(user, text));
                replyMessage.setReplyMarkup(getMenuKeyboard());
                break;

            // user is adding a new note
            case ADDNOTE:

                replyMessage.setText(handleAddNote(user, text));
                replyMessage.setReplyMarkup(getMenuKeyboard());
                break;
        }
    }

    // simple button handlers
    private void handleMessage(User user, String message_text, EditMessageText replyMessage) {

        // on /addList
        if (message_text.startsWith(requestCommands[1])) {

            onAddList(user, replyMessage);

            // on /addNote
        } else if (message_text.startsWith(requestCommands[2])) {

            onAddNote(user, replyMessage);

            // on /getLists
        } else if (message_text.startsWith(requestCommands[3])) {

            onGetLists(user, replyMessage);

            // on /chooseList
        } else if (message_text.startsWith(requestCommands[4])) {

            onChooseList(user, replyMessage);

            // on /getNotes
        } else if (message_text.startsWith(requestCommands[5])) {

            onGetNotes(user, replyMessage);

            // on /setDone
        } else if (message_text.startsWith(requestCommands[6])) {

            onSetNoteDone(user, replyMessage);

            // on /removeList
        } else if (message_text.startsWith(requestCommands[7])) {

            onRemoveList(user, replyMessage);

            // on /removeNote
        } else if (message_text.startsWith(requestCommands[8])) {

            onRemoveNote(user, replyMessage);

            // on /cancel
        } else if (message_text.startsWith(requestCommands[10])) {

            onCancel(replyMessage, user);

        } else {

            // just gave a menu to user
            onMenu(replyMessage);
        }
    }


    //----------------------- wait handlers
    private String handleRemoveNote(User user, String text) {

        //reset user statement
        user.setCurrentStatement(NOTHING);

        // update user
        userDAO.update(user);

        // find note
        Note note = null;
        for (Note nt : user.getCurrentList().getNotes()) {

            if (nt.getText().equals(text))
                note = nt;
        }

        // if not found
        if (note == null)
            return "Such note does not exists in current list";

        // remove note
        noteDAO.remove(note);

        return "Note " + note.getText() + " removed";
    }

    private String handleRemoveList(User user, String text) {

        //reset user statement
        user.setCurrentStatement(NOTHING);

        //update user
        userDAO.update(user);

        NoteList toRemove = null;

        //find list
        for (NoteList nl : user.getNoteLists())
            if (nl.getName().equals(text))
                toRemove = nl;

        // check if not found
        if (toRemove == null)
            return "Such list does not exists";

        if (user.getCurrentList() == toRemove) {
            user.setCurrentList(null);
            userDAO.update(user);
        }

        // remove list
        noteListDAO.remove(toRemove);
        return "List " + text + " removed";
    }

    private String handleSetDone(User user, String text) {

        //reset user statement
        user.setCurrentStatement(NOTHING);

        // find note
        Note note = null;
        for (Note nt : user.getCurrentList().getNotes()) {

            if (nt.getText().equals(text))
                note = nt;
        }

        // if not found
        if (note == null)
            return "Such note does not exists in current list";

        // set done
        note.setDone(true);

        //update user
        userDAO.update(user);

        return "Note" + note.getText() + " is done now";
    }

    private String handleAddNote(User user, String text) {

        //reset user statement
        user.setCurrentStatement(NOTHING);

        // unique check
        for (Note n : user.getCurrentList().getNotes()) {

            if (n.getText().equals(text)) {
                userDAO.update(user);
                return "Such note already exists";
            }
        }

        //create new note
        Note note = new Note(text, false, user.getCurrentList());

        // add note to list
        user.getCurrentList().getNotes().add(note);
        userDAO.update(user);

        return "Note " + text + " added";

    }

    private String handleChooseList(User user, String text) {

        //reset user statement
        user.setCurrentStatement(NOTHING);

        // find list
        NoteList noteList = null;
        for (NoteList nl : user.getNoteLists())
            if (nl.getName().equals(text))
                noteList = nl;

        // if found
        if (noteList != null) {

            // set list to current
            user.setCurrentList(noteList);

            //update user
            userDAO.update(user);
            return "List " + noteList.getName() + " chosen";
        }
        userDAO.update(user);
        return "List not found";
    }

    private String handleAddList(User user, String text) {

        // reset statement
        user.setCurrentStatement(NOTHING);

        // check if list already exists
        for (NoteList n : user.getNoteLists()) {
            if (n.getName().equals(text))
                return "Such list already exists";
        }

        NoteList newList = new NoteList(text, user, null);
        // create and add a new list
        user.getNoteLists().add(newList);
        user.setCurrentList(newList);
        // update user
        userDAO.update(user);
        return "list " + text + " created";

    }


    //----------------------- command handlers

    private void onCancel(EditMessageText message, User user) {

        user.setCurrentStatement(NOTHING);
        userDAO.update(user);

        message.setText("Action canceled");
        message.setReplyMarkup(getMenuKeyboard());
    }

    private void onSendMenu(SendMessage message) {

        message.setText("Menu");
        message.setReplyMarkup(getMenuKeyboard());
    }

    private void onMenu(EditMessageText message) {

        message.setText("Menu");
        message.setReplyMarkup(getMenuKeyboard());
    }

    private void onRemoveNote(User user, EditMessageText message) {

        //get current list
        NoteList currentList = user.getCurrentList();

        if (currentList == null) {
            if (user.getNoteLists().isEmpty()) {
                message.setText("Add list at first");
                message.setReplyMarkup(getSingleAddListButton());
                return;
            }

            user.setCurrentStatement(CHOOSELIST);
            userDAO.update(user);
            message.setText(PLEASE_CHOOSE_LIST);
            message.setReplyMarkup(getListKeyboard(user.getNoteLists()));
            return;
        }

        // check if note list is empty
        if (currentList.getNotes().isEmpty()) {

            message.setText("This note list is empty");
            message.setReplyMarkup(getMenuKeyboard());
            return;
        }

        //set user statement
        user.setCurrentStatement(REMOVENOTE);

        //upd user
        userDAO.update(user);

        message.setText("Enter note name");
        message.setReplyMarkup(getNoteKeyboard(currentList.getNotes()));
    }

    private void onRemoveList(User user, EditMessageText message) {

        // check if user have lists
        if (user.getNoteLists().isEmpty()) {

            message.setText("You have no lists yet");
            message.setReplyMarkup(getMenuKeyboard());
            return;
        }

        // set user statement
        user.setCurrentStatement(REMOVELIST);

        // update user
        userDAO.update(user);
        message.setText("Please choose list");
        message.setReplyMarkup(getListKeyboard(user.getNoteLists()));

    }

    private void onGetNotes(User user, EditMessageText message) {

        //get current list
        NoteList currentList = user.getCurrentList();

        if (currentList == null) {
            if (user.getNoteLists().isEmpty()) {
                message.setText("Add list at first");
                message.setReplyMarkup(getSingleAddListButton());
                return;
            }

            user.setCurrentStatement(CHOOSELIST);
            userDAO.update(user);
            message.setText(PLEASE_CHOOSE_LIST);
            message.setReplyMarkup(getListKeyboard(user.getNoteLists()));
            return;
        }

        StringBuilder result = new StringBuilder();
        result
                .append(currentList.getName())
                .append(":\n");

        for (Note nt : currentList.getNotes()) {

            if (nt.isDone())
                result.append("✔️ ");
            else
                result.append("❗ ");

            result
                    .append(nt.getText())
                    .append("\n");

        }

        message.setText(result.toString());
        message.setReplyMarkup(getNoteOptionsKeyboard());
    }

    private void onAddNote(User user, EditMessageText message) {

        //get current list
        NoteList currentList = user.getCurrentList();

        if (currentList == null) {
            message.setText(PLEASE_CHOOSE_LIST);
            message.setReplyMarkup(getListKeyboard(user.getNoteLists()));
            return;
        }

        // set user statement
        user.setCurrentStatement(ADDNOTE);

        // update user
        userDAO.update(user);

        message.setText("Enter new note");
    }

    private void onSetNoteDone(User user, EditMessageText message) {

        //get current list
        NoteList currentList = user.getCurrentList();

        if (currentList == null) {
            if (user.getNoteLists().isEmpty()) {
                message.setText("Add list at first");
                message.setReplyMarkup(getSingleAddListButton());
                return;
            }

            user.setCurrentStatement(CHOOSELIST);
            userDAO.update(user);
            message.setText(PLEASE_CHOOSE_LIST);
            message.setReplyMarkup(getListKeyboard(user.getNoteLists()));
            return;
        }
        System.out.println("\n\nUser has current list");
        // check if note list is empty
        if (currentList.getNotes().isEmpty()) {

            message.setText("This note list is empty");
            message.setReplyMarkup(getMenuKeyboard());
            return;
        }
        System.out.println("\n\nUser's current list is not empty");

        //set user statement
        user.setCurrentStatement(SETDONE);
        System.out.println("\n\nset user statement to " + SETDONE);

        //upd user
        userDAO.update(user);
        System.out.println("\n\nupd user");

        message.setText("Enter note name");
        message.setReplyMarkup(getNoteKeyboard(currentList.getNotes()));
    }

    private void onChooseList(User user, EditMessageText message) {

        if (user.getNoteLists().isEmpty()) {
            message.setText("You have no lists yet");
            message.setReplyMarkup(getMenuKeyboard());
            return;
        }

        // set user statement
        user.setCurrentStatement(CHOOSELIST);

        //update user
        userDAO.update(user);

        message.setText("Enter list name");
        message.setReplyMarkup(getListKeyboard(user.getNoteLists()));

    }

    private void onGetLists(User user, EditMessageText message) {

        // build an answer
        StringBuilder sb = new StringBuilder();
        for (NoteList list : user.getNoteLists()) {
            sb.append(list.getName())
                    .append("\n");
        }
        if (sb.length() == 0)
            sb.append("Your have no lists yet");

        message.setText(sb.toString());
        message.setReplyMarkup(getListOptionsKeyboard());

    }

    private void onAddList(User user, EditMessageText message) {

        // set statement to wait until input
        user.setCurrentStatement(ADDLIST);
        userDAO.update(user);
        message.setText("Enter new list name");
    }

    private void onRegister(long chat_id, SendMessage message) {

        User user = new User(chat_id, null, null);
        userDAO.add(user);

        message.setText(
                "You have been registered in a system\n" +
                        "Menu");
        message.setReplyMarkup(getMenuKeyboard());

    }

    //----------------------- keyboards

    private InlineKeyboardMarkup getMenuKeyboard() {

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Lists").setCallbackData(requestCommands[3]));
        rowInline.add(new InlineKeyboardButton().setText("Notes").setCallbackData(requestCommands[5]));

        // Set the keyboard to the markup
        rowsInline.add(rowInline);

        // Add it to the message
        markup.setKeyboard(rowsInline);

        return markup;
    }

    private InlineKeyboardMarkup getSingleAddListButton() {

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Add List").setCallbackData(requestCommands[1]));

        // Set the keyboard to the markup
        rowsInline.add(rowInline);

        // Add it to the message
        markup.setKeyboard(rowsInline);

        return markup;
    }

    private InlineKeyboardMarkup getNoteKeyboard(List<Note> notes) {

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (Note n : notes) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(new InlineKeyboardButton().setText(n.getText()).setCallbackData(n.getText()));

            rowsInline.add(rowInline);
        }


        rowsInline.add(
                new ArrayList<InlineKeyboardButton>() {{
                    add(
                            new InlineKeyboardButton()
                                    .setText("Cancel")
                                    .setCallbackData(requestCommands[10]));
                }});

        markup.setKeyboard(rowsInline);
        return markup;

    }

    private InlineKeyboardMarkup getListKeyboard(List<NoteList> noteLists) {

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (NoteList nl : noteLists) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(new InlineKeyboardButton().setText(nl.getName()).setCallbackData(nl.getName()));

            rowsInline.add(rowInline);
        }

        rowsInline.add(
                new ArrayList<InlineKeyboardButton>() {{
                    add(
                            new InlineKeyboardButton()
                                    .setText("Cancel")
                                    .setCallbackData(requestCommands[10]));
                }});

        markup.setKeyboard(rowsInline);
        return markup;
    }

    private InlineKeyboardMarkup getListOptionsKeyboard() {

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(
                new ArrayList<InlineKeyboardButton>() {{
                    add(
                            new InlineKeyboardButton()
                                    .setText("Add")
                                    .setCallbackData(requestCommands[1])
                    );

                    add(
                            new InlineKeyboardButton()
                                    .setText("Choose")
                                    .setCallbackData(requestCommands[4])
                    );
                }}
        );

        rowsInline.add(
                new ArrayList<InlineKeyboardButton>() {{
                    add(
                            new InlineKeyboardButton()
                                    .setText("Remove")
                                    .setCallbackData(requestCommands[7])
                    );

                    add(
                            new InlineKeyboardButton()
                                    .setText("Menu")
                                    .setCallbackData(requestCommands[9])
                    );
                }}
        );

        markup.setKeyboard(rowsInline);

        return markup;
    }

    private InlineKeyboardMarkup getNoteOptionsKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(
                new ArrayList<InlineKeyboardButton>() {{
                    add(
                            new InlineKeyboardButton()
                                    .setText("Add")
                                    .setCallbackData(requestCommands[2])
                    );

                    add(
                            new InlineKeyboardButton()
                                    .setText("Set Done")
                                    .setCallbackData(requestCommands[6])
                    );
                }}
        );

        rowsInline.add(
                new ArrayList<InlineKeyboardButton>() {{
                    add(
                            new InlineKeyboardButton()
                                    .setText("Remove")
                                    .setCallbackData(requestCommands[8])
                    );

                    add(
                            new InlineKeyboardButton()
                                    .setText("Menu")
                                    .setCallbackData(requestCommands[9])
                    );
                }}
        );

        markup.setKeyboard(rowsInline);

        return markup;
    }


    @Override
    public String getBotUsername() {
        return "NessTodoListBot";
    }

    @Override
    public String getBotToken() {
        return "330574774:AAG4KzepsOTtody6PxVoKvqyG9jOl8081Dg";
    }
}
