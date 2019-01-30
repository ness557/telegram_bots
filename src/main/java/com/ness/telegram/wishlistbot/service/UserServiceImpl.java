package com.ness.telegram.wishlistbot.service;

import com.ness.telegram.wishlistbot.model.User;
import com.ness.telegram.wishlistbot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Override
    public void save(User user) {
        repository.save(user);
    }

    @Override
    public void delete(User user) {
        repository.delete(user);
    }

    @Override
    public User get(Long id) {
        return repository.getOne(id);
    }

    @Override
    public User findByChatId(Integer chatId) {
        return repository.findByChatId(chatId);
    }
}