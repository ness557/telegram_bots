package com.ness.telegram.wishlistbot.service;

import com.ness.telegram.wishlistbot.model.User;
import com.ness.telegram.wishlistbot.model.Wish;

public interface WishService {
    void save(Wish wish);
    void delete(Wish wish);
    void delete(Long id);
    Wish get(Long id);
    Wish findByLabelAndUser(String label, User user);    
}