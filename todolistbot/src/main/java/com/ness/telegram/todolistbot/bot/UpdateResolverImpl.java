package com.ness.telegram.todolistbot.bot;

import com.ness.telegram.todolistbot.model.Note;
import com.ness.telegram.todolistbot.model.NoteList;
import com.ness.telegram.todolistbot.model.Statement;
import com.ness.telegram.todolistbot.model.User;
import com.ness.telegram.todolistbot.service.NoteListService;
import com.ness.telegram.todolistbot.service.NoteService;
import com.ness.telegram.todolistbot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.join;

@Service
@Transactional
@Slf4j
public class UpdateResolverImpl implements UpdateResolver {

    @Autowired
    private UserService userService;

    @Autowired
    private NoteListService noteListService;

    @Autowired
    private NoteService noteService;

    @Override
    public BotApiMethod resolve(Update update) {
        Message message;

        Integer messageId = null;
        Long chatId;
        String text;

        if (update.hasMessage()) {
            message = update.getMessage();
            text = message.getText();
        } else if (update.hasCallbackQuery()) {
            message = update.getCallbackQuery().getMessage();
            messageId = message.getMessageId();
            text = update.getCallbackQuery().getData();

        } else {
            throw new UnsupportedOperationException("Wrong message type");
        }

        chatId = message.getChatId();

        //get user or register new
        User user = userService
                .findByChatId(chatId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setChatId(chatId);
                    newUser.setCurrentStatement(Statement.DEFAULT);
                    userService.save(newUser);

                    return newUser;
                });

        Command command = Command.ofText(text);

        BotApiMethod result = null;
        switch (command) {
            case LISTS:
                result = getLists(user, messageId);
                break;

            case NOTES:
                result = getNotes(user, messageId);
                break;

            case ADD_LIST:
                result = addList(user, messageId);
                break;

            case REMOVE_LIST:
                result = removeList(user, messageId);
                break;

            case ADD_NOTE:
                result = addNote(user, messageId);
                break;

            case REMOVE_NOTE:
                result = removeNote(user, messageId);
                break;

            case SET_LIST:
                result = setList(user, messageId);
                break;

            case SET_DONE:
                result = setNoteDone(user, messageId);
                break;

            case CANCEL:
                result = cancel(user, messageId);
                break;

            case MENU:
                result = menu(user, messageId);
                break;

            case HELP:
                result = help(user);
                break;

            case NONE:
                if (user.getCurrentStatement() == Statement.DEFAULT) {
                    result = menu(user);
                    break;
                }

                result = resolveStatement(user, messageId, text);
                break;
        }

        return result;

    }

    private BotApiMethod resolveStatement(User user, Integer messageId, String text) {
        BotApiMethod result = null;

        Statement statement = user.getCurrentStatement();
        switch (statement) {

            case ADDLIST:
                result = addListStatement(user, text);
                break;
            case CHOOSELIST:
                result = chooseListStatement(user, text, messageId);
                break;
            case ADDNOTE:
                result = addNoteStatement(user, text);
                break;
            case SETDONE:
                result = setNoteDoneStatement(user, text, messageId);
                break;
            case REMOVELIST:
                result = removeListStatement(user, text, messageId);
                break;
            case REMOVENOTE:
                result = removeNoteStatement(user, text, messageId);
                break;
        }

        return result;
    }

    private BotApiMethod removeNoteStatement(User user, String text, Integer messageId) {
        EditMessageText response = new EditMessageText()
                .setChatId(user.getChatId())
                .setMessageId(messageId)
                .setReplyMarkup(ReplyMarkups.menuKeyboard())
                .setText(ReplyConstants.MESSAGE_NOTE_NOT_FOUND);

        user.setCurrentStatement(Statement.DEFAULT);
        userService.save(user);

        noteService.findAllByNoteList(user.getCurrentList()).stream()
                .filter(note -> note.getText().equals(text))
                .findAny()
                .ifPresent(note -> {
                    noteService.delete(note);
                    response.setText("Note " + note.getText() + " removed");
                });

        return response;
    }

    private BotApiMethod removeListStatement(User user, String text, Integer messageId) {
        EditMessageText response = new EditMessageText()
                .setChatId(user.getChatId())
                .setMessageId(messageId)
                .setReplyMarkup(ReplyMarkups.menuKeyboard())
                .setText(ReplyConstants.MESSAGE_LIST_NOT_FOUND);

        user.setCurrentStatement(Statement.DEFAULT);
        userService.save(user);

        noteListService.getAllByUser(user).stream()
                .filter(list -> list.getName().equals(text))
                .findAny()
                .ifPresent(list -> {
                    if (list.getUser().getCurrentList().equals(list)) {
                        list.getUser().setCurrentList(null);
                    }
                    noteListService.delete(list);
                    response.setText("List " + list.getName() + " removed");
                });

        return response;
    }

    private BotApiMethod setNoteDoneStatement(User user, String text, Integer messageId) {
        EditMessageText response = new EditMessageText()
                .setChatId(user.getChatId())
                .setMessageId(messageId)
                .setReplyMarkup(ReplyMarkups.menuKeyboard())
                .setText(ReplyConstants.MESSAGE_NOTE_NOT_FOUND);

        user.setCurrentStatement(Statement.DEFAULT);
        userService.save(user);

        noteService.findAllByNoteList(user.getCurrentList()).stream()
                .filter(note -> note.getText().equals(text))
                .findAny()
                .ifPresent(note -> {
                    note.setDone(true);
                    noteService.save(note);
                    response.setText("Note " + note.getText() + " is done now");
                });

        return response;
    }

    private BotApiMethod addNoteStatement(User user, String text) {
        SendMessage response = new SendMessage()
                .setChatId(user.getChatId())
                .setReplyMarkup(ReplyMarkups.menuKeyboard())
                .setText(ReplyConstants.MESSAGE_NOTE_ALREADY_EXISTS);

        if(text.getBytes().length > 64){
            response.setText(ReplyConstants.MESSAGE_TOO_LONG);
            return response;
        }

        user.setCurrentStatement(Statement.DEFAULT);
        userService.save(user);

        NoteList currentList = user.getCurrentList();

        if (noteService.findAllByNoteList(currentList).stream()
                .noneMatch(note -> note.getText().equals(text))) {
            Note note = new Note();
            note.setNoteList(currentList);
            note.setText(text);

            noteService.save(note);

            response.setText("Note " + text + " added");
        }

        return response;
    }

    private BotApiMethod chooseListStatement(User user, String text, Integer messageId) {
        EditMessageText response = new EditMessageText()
                .setChatId(user.getChatId())
                .setMessageId(messageId)
                .setReplyMarkup(ReplyMarkups.menuKeyboard())
                .setText(ReplyConstants.MESSAGE_LIST_NOT_FOUND);

        user.setCurrentStatement(Statement.DEFAULT);
        userService.save(user);

        noteListService.getAllByUser(user).stream()
                .filter(list -> list.getName().equals(text))
                .findFirst()
                .ifPresent(list -> {
                    user.setCurrentList(list);
                    response.setText("List " + text + " chosen");
                });

        return response;
    }

    private BotApiMethod addListStatement(User user, String text) {
        SendMessage response = new SendMessage()
                .setChatId(user.getChatId())
                .setReplyMarkup(ReplyMarkups.menuKeyboard());

        if(text.getBytes().length > 64){
            response.setText(ReplyConstants.MESSAGE_TOO_LONG);
            return response;
        }

        user.setCurrentStatement(Statement.DEFAULT);

        List<NoteList> lists = noteListService.getAllByUser(user);
        if (lists.stream().anyMatch(l -> l.getName().equals(text))) {
            response.setText(ReplyConstants.MESSAGE_LIST_ALREADY_EXISTS);
            userService.save(user);
            return response;
        }
        NoteList noteList = new NoteList();
        noteList.setName(text);
        noteList.setUser(user);

        user.setCurrentList(noteList);

        noteListService.save(noteList);
        userService.save(user);

        response.setText("List " + text + " added");

        return response;
    }

    private BotApiMethod help(User user) {
        return new SendMessage(user.getChatId(), ReplyConstants.MESSAGE_HELP);
    }

    private BotApiMethod menu(User user, Integer messageId) {

        return new EditMessageText()
                .setChatId(user.getChatId())
                .setMessageId(messageId)
                .setText(ReplyConstants.MESSAGE_MENU)
                .setReplyMarkup(ReplyMarkups.menuKeyboard());
    }

    private BotApiMethod menu(User user) {

        return new SendMessage()
                .setChatId(user.getChatId())
                .setText(ReplyConstants.MESSAGE_MENU)
                .setReplyMarkup(ReplyMarkups.menuKeyboard());
    }

    private BotApiMethod cancel(User user, Integer messageId) {
        EditMessageText response = new EditMessageText()
                .setChatId(user.getChatId())
                .setMessageId(messageId)
                .setReplyMarkup(ReplyMarkups.menuKeyboard())
                .setText(ReplyConstants.MESSAGE_CANCEL);

        user.setCurrentStatement(Statement.DEFAULT);
        userService.save(user);

        return response;
    }

    private BotApiMethod setNoteDone(User user, Integer messageId) {
        EditMessageText response = new EditMessageText()
                .setChatId(user.getChatId())
                .setMessageId(messageId);

        NoteList currentList = user.getCurrentList();

        if (currentList == null) {
            response.setText(ReplyConstants.MESSAGE_NOT_CHOSEN);
            response.setReplyMarkup(ReplyMarkups.showListsButton());

            return response;
        }

        user.setCurrentStatement(Statement.SETDONE);
        userService.save(user);

        response.setText(ReplyConstants.MESSAGE_CHOOSE_NOTE);
        response.setReplyMarkup(ReplyMarkups.chooseNoteKeyboard(currentList.getNotes()));

        return response;
    }

    private BotApiMethod setList(User user, Integer messageId) {
        EditMessageText response = new EditMessageText()
                .setChatId(user.getChatId())
                .setMessageId(messageId);

        List<NoteList> lists = noteListService.getAllByUser(user);

        user.setCurrentStatement(Statement.CHOOSELIST);
        userService.save(user);

        response.setText(ReplyConstants.MESSAGE_CHOOSE_LIST);
        response.setReplyMarkup(ReplyMarkups.chooseListsKeyboard(lists));

        return response;
    }

    private BotApiMethod removeNote(User user, Integer messageId) {
        EditMessageText response = new EditMessageText()
                .setChatId(user.getChatId())
                .setMessageId(messageId);

        NoteList currentList = user.getCurrentList();

        if (currentList == null) {
            response.setText(ReplyConstants.MESSAGE_NOT_CHOSEN);
            response.setReplyMarkup(ReplyMarkups.showListsButton());

            return response;
        }

        user.setCurrentStatement(Statement.REMOVENOTE);
        userService.save(user);

        response.setText(ReplyConstants.MESSAGE_REMOVE_NOTE);
        response.setReplyMarkup(ReplyMarkups.chooseNoteKeyboard(currentList.getNotes()));

        return response;
    }

    private BotApiMethod removeList(User user, Integer messageId) {
        EditMessageText response = new EditMessageText()
                .setChatId(user.getChatId())
                .setMessageId(messageId);

        List<NoteList> lists = noteListService.getAllByUser(user);
        if (lists.isEmpty()) {
            response.setText(ReplyConstants.MESSAGE_NO_LISTS);
            response.setReplyMarkup(ReplyMarkups.menuKeyboard());
        }

        user.setCurrentStatement(Statement.REMOVELIST);
        userService.save(user);

        response.setText(ReplyConstants.MESSAGE_REMOVE_LIST);
        response.setReplyMarkup(ReplyMarkups.chooseListsKeyboard(lists));

        return response;
    }

    private BotApiMethod getLists(User user, Integer messageId) {
        EditMessageText response = new EditMessageText()
                .setChatId(user.getChatId())
                .setMessageId(messageId);

        String lists = noteListService.getAllByUser(user).stream()
                .map(NoteList::getName)
                .collect(Collectors.joining("\n"));

        if (StringUtils.isEmpty(lists)) {
            response.setText(ReplyConstants.MESSAGE_NO_LISTS);
            response.setReplyMarkup(ReplyMarkups.listsActionsKeyboard(true));
            return response;
        }

        response.setText("Lists:\n" + lists);
        response.setReplyMarkup(ReplyMarkups.listsActionsKeyboard(false));

        return response;
    }

    private BotApiMethod getNotes(User user, Integer messageId) {
        EditMessageText response = new EditMessageText()
                .setChatId(user.getChatId())
                .setMessageId(messageId);

        NoteList currentList = user.getCurrentList();

        if (currentList == null) {
            response.setText(ReplyConstants.MESSAGE_NOT_CHOSEN);
            response.setReplyMarkup(ReplyMarkups.showListsButton());

            return response;
        }

        String notes = noteService.findAllByNoteList(currentList).stream()
                .map(note -> join(" ", note.isDone() ? "✔️" : "❗ ", note.getText()))
                .collect(Collectors.joining("\n"));

        if (StringUtils.isEmpty(notes)) {
            response.setText(ReplyConstants.MESSAGE_NO_NOTES);
            response.setReplyMarkup(ReplyMarkups.notesActionsKeyboard(true));

            return response;
        }

        response.setText(currentList.getName() + ":\n" + notes);
        response.setReplyMarkup(ReplyMarkups.notesActionsKeyboard(false));

        return response;
    }

    private BotApiMethod addList(User user, Integer messageId) {
        EditMessageText response = new EditMessageText()
                .setChatId(user.getChatId())
                .setMessageId(messageId)
                .setText(ReplyConstants.MESSAGE_ADD_LIST);

        user.setCurrentStatement(Statement.ADDLIST);
        userService.save(user);

        return response;
    }

    private BotApiMethod addNote(User user, Integer messageId) {
        EditMessageText response = new EditMessageText()
                .setChatId(user.getChatId())
                .setMessageId(messageId);

        NoteList currentList = user.getCurrentList();

        if (currentList == null) {
            response.setText(ReplyConstants.MESSAGE_NOT_CHOSEN);
            response.setReplyMarkup(ReplyMarkups.showListsButton());

            return response;
        }

        user.setCurrentStatement(Statement.ADDNOTE);
        userService.save(user);

        response.setText(ReplyConstants.MESSAGE_ADD_NOTE);

        return response;
    }
}
