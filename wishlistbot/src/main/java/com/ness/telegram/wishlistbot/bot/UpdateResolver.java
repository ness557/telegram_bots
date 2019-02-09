package com.ness.telegram.wishlistbot.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateResolver {
    SendMessage resolve(Update update);
}
