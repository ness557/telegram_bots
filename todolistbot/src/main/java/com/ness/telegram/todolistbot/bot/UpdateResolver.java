package com.ness.telegram.todolistbot.bot;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateResolver {
    BotApiMethod resolve(Update update);
}
