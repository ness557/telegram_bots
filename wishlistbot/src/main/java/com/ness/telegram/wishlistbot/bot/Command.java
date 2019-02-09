package com.ness.telegram.wishlistbot.bot;

import lombok.Getter;

import java.util.Arrays;

public enum Command {
    LIST("/list"),
    ADD("/add"),
    EDIT("/edit"),
    REMOVE("/remove"),
    HELP("/help"),
    CANCEL("/cancel"),
    SKIP("/skip"),
    ERROR("");

    @Getter
    private String text;

    Command(String text) {
        this.text = text;
    }

    public static Command ofText(String text) {
        return Arrays.stream(Command.values())
                        .filter(e -> e.getText().equals(text)).findFirst()
                        .orElse(Command.ERROR);
    }
}
