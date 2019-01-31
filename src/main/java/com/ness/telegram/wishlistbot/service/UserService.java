package com.ness.telegram.wishlistbot.service;

import java.util.Optional;
import com.ness.telegram.wishlistbot.model.User;

public interface UserService {
    void save(User user);
    void delete(User user);
    User get(Long id);
    Optional<User> findByChatId(Long chatId);
}