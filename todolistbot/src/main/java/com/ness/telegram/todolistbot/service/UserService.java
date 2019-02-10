package com.ness.telegram.todolistbot.service;

import com.ness.telegram.todolistbot.model.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByChatId(Long chatId);
    void save(User user);
    Optional<User> findById(Long id);
}
