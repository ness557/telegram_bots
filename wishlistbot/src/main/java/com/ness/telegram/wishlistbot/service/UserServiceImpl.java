package com.ness.telegram.wishlistbot.service;

import java.util.Optional;
import com.ness.telegram.wishlistbot.model.User;
import com.ness.telegram.wishlistbot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Override
    public void save(User user) {
        log.info("Saving user [{}]", user);
        repository.save(user);
        log.info("Saved");
    }

    @Override
    public void delete(User user) {
        log.info("Deleting user [{}]", user);
        repository.delete(user);
        log.info("Deleted");
    }

    @Override
    public User get(Long id) {
        log.info("Getting user by id [{}]", id);
        User user = repository.getOne(id);
        log.info("Got user [{}]", user);
        return user;
    }

    @Override
    public Optional<User> findByChatId(Long chatId) {
        log.info("Finding user by chat id [{}]", chatId);
        Optional<User> user = repository.findByChatId(chatId);
        log.info("Found user [{}]", user);
        return user;
    }
}