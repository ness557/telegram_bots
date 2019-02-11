package com.ness.telegram.todolistbot.bot;

import com.ness.telegram.todolistbot.model.Note;
import com.ness.telegram.todolistbot.model.NoteList;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.ness.telegram.todolistbot.bot.Command.*;
import static com.ness.telegram.todolistbot.bot.ReplyConstants.*;

public class ReplyMarkups {

    public static InlineKeyboardMarkup listsActionsKeyboard(Boolean isEmpty) {

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> column1 = new ArrayList<>();

        InlineKeyboardButton addButton = new InlineKeyboardButton(BUTTON_ADD).setCallbackData(ADD_LIST.getText());
        InlineKeyboardButton menuButton = new InlineKeyboardButton(ReplyConstants.BUTTON_MENU).setCallbackData(MENU.getText());

        column1.add(addButton);
        if (!isEmpty) {
            InlineKeyboardButton chooseButton = new InlineKeyboardButton(BUTTON_CHOOSE).setCallbackData(SET_LIST.getText());
            InlineKeyboardButton removeButton = new InlineKeyboardButton(BUTTON_REMOVE).setCallbackData(REMOVE_LIST.getText());

            column1.add(chooseButton);

            List<InlineKeyboardButton> column2 = new ArrayList<>();
            column2.add(removeButton);
            column2.add(menuButton);

            rows.add(column2);
        } else {
            column1.add(menuButton);
        }

        rows.add(0, column1);
        keyboard.setKeyboard(rows);

        return keyboard;
    }

    public static InlineKeyboardMarkup showListsButton() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(Arrays.asList(new InlineKeyboardButton(ReplyConstants.BUTTON_LISTS).setCallbackData(LISTS.getText())));
        keyboard.setKeyboard(rows);

        return keyboard;
    }

    public static InlineKeyboardMarkup notesActionsKeyboard(Boolean isEmpty) {

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> column1 = new ArrayList<>();

        InlineKeyboardButton addButton = new InlineKeyboardButton(BUTTON_ADD).setCallbackData(ADD_NOTE.getText());
        InlineKeyboardButton menuButton = new InlineKeyboardButton(ReplyConstants.BUTTON_MENU).setCallbackData(MENU.getText());

        column1.add(addButton);
        if (!isEmpty) {
            InlineKeyboardButton setDoneButton = new InlineKeyboardButton(ReplyConstants.BUTTON_SETDONE).setCallbackData(SET_DONE.getText());
            InlineKeyboardButton removeButton = new InlineKeyboardButton(BUTTON_REMOVE).setCallbackData(REMOVE_NOTE.getText());

            column1.add(setDoneButton);

            List<InlineKeyboardButton> column2 = new ArrayList<>();
            column2.add(removeButton);
            column2.add(menuButton);

            rows.add(column2);
        } else {
            column1.add(menuButton);
        }

        rows.add(0, column1);
        keyboard.setKeyboard(rows);

        return keyboard;
    }

    public static InlineKeyboardMarkup menuKeyboard() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardButton listsButton = new InlineKeyboardButton(BUTTON_LISTS).setCallbackData(LISTS.getText());
        InlineKeyboardButton notesButton = new InlineKeyboardButton(BUTTON_NOTES).setCallbackData(NOTES.getText());

        rows.add(Arrays.asList(listsButton, notesButton));

        keyboard.setKeyboard(rows);

        return keyboard;
    }

    public static InlineKeyboardMarkup chooseListsKeyboard(List<NoteList> lists) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = lists.stream()
                .map(list ->
                        Arrays.asList(new InlineKeyboardButton(list.getName())
                                .setCallbackData(list.getName())))
                .collect(Collectors.toList());

        rows.add(Arrays.asList(
                new InlineKeyboardButton(ReplyConstants.BUTTON_CANCEL)
                        .setCallbackData(CANCEL.getText())));

        keyboard.setKeyboard(rows);

        return keyboard;
    }

    public static InlineKeyboardMarkup chooseNoteKeyboard(List<Note> notes) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = notes.stream()
                .map(note ->
                        Arrays.asList(new InlineKeyboardButton(note.getText())
                                .setCallbackData(note.getText())))
                .collect(Collectors.toList());

        rows.add(Arrays.asList(
                new InlineKeyboardButton(ReplyConstants.BUTTON_CANCEL)
                        .setCallbackData(CANCEL.getText())));

        keyboard.setKeyboard(rows);

        return keyboard;
    }
}
