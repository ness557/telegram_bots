package com.ness.telegram.wishlistbot.service;

import java.util.List;
import com.ness.telegram.wishlistbot.model.User;
import com.ness.telegram.wishlistbot.model.Wish;
import com.ness.telegram.wishlistbot.repository.WishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WishServiceImpl implements WishService {

    @Autowired
    private WishRepository repository;

    @Override
    public void save(Wish wish) {
        log.info("Saving wish [{}]", wish);
        repository.save(wish);
        log.info("Saved");
    }

    @Override
    public void delete(Wish wish) {
        log.info("Deleting wish [{}]", wish);
        repository.delete(wish);
        log.info("Deleted");
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting wish by id [{}]", id);
        repository.deleteById(id);
        log.info("Deleted");
    }

    @Override
    public Wish get(Long id) {
        log.info("Getting wish by id [{}]", id);
        Wish wish = repository.getOne(id);
        log.info("Got wish [{}]", wish);
        return wish;
    }

    @Override
    public Wish findByLabelAndUser(String label, User user) {
        log.info("Finding wish by label [{}] and user [{}]", label, user);
        Wish wish = repository.findByLabelAndUser(label, user);
        log.info("Found user [{}]", wish);
        return wish;
    }

    @Override
    public Wish findByLabelAndUserChatId(String label, Integer chatId) {
        log.info("Finding wish by label [{}] and user chat id [{}]", label, chatId);
        Wish wish = repository.findByLabelAndUserChatId(label, chatId);
        log.info("Found wish [{}]", wish);
        return wish;
    }

    @Override
    public List<Wish> findByUserChatId(Integer chatId) {
        log.info("Finding wishes by user chat id [{}]", chatId);
        List<Wish> wishes = repository.findByUserChatId(chatId);
        log.info("Found wishes {}", wishes);
        return wishes;
    }
}