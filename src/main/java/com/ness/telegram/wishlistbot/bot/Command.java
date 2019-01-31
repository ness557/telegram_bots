package com.ness.telegram.wishlistbot.bot;

import java.util.Arrays;
import lombok.Getter;

/**
 * Command
 */
public enum Command {
    LIST("/list"),
    ADD("/add"),
    REMOVE("/remove"),
    HELP("/help"),
    ABORT("/abort"),
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
