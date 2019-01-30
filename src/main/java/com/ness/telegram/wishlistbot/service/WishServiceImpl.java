package com.ness.telegram.wishlistbot.service;

import com.ness.telegram.wishlistbot.model.User;
import com.ness.telegram.wishlistbot.model.Wish;
import com.ness.telegram.wishlistbot.repository.WishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WishServiceImpl implements WishService {

    @Autowired
    private WishRepository repository;

    @Override
    public void save(Wish wish) {
        repository.save(wish);
    }

    @Override
    public void delete(Wish wish) {
        repository.delete(wish);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Wish get(Long id) {
        return repository.getOne(id);
    }

    @Override
    public Wish findByLabelAndUser(String label, User user) {
        return findByLabelAndUser(label, user);
    } 
}