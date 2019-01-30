package com.ness.telegram.wishlistbot.service;

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
}