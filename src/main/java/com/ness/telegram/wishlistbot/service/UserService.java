package com.ness.telegram.wishlistbot.service;

import com.ness.telegram.wishlistbot.model.User;

public interface UserService {
    void save(User user);
    void delete(User user);
    User get(Long id);
    User findByChatId(Integer chatId);
}