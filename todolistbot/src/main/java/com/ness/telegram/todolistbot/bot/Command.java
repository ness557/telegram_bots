package com.ness.telegram.todolistbot.bot;

import lombok.Getter;

import java.util.Arrays;

public enum Command {
    LISTS("/lists"),
    NOTES("/notes"),
    ADD_LIST("/addlist"),
    ADD_NOTE("/addnote"),
    REMOVE_LIST("/removelist"),
    REMOVE_NOTE("/removenote"),
    SET_LIST("/chooselist"),
    SET_DONE("/setdone"),
    HELP("/help"),
    CANCEL("/cancel"),
    MENU("/menu"),
    NONE("");

    @Getter
    private String text;

    Command(String text){
        this.text = text;
    }

    public static Command ofText(String text) {
        return Arrays.stream(Command.values())
                .filter(e -> e.getText().equals(text)).findFirst()
                .orElse(Command.NONE);
    }
}
