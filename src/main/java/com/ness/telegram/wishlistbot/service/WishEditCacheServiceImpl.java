package com.ness.telegram.wishlistbot.service;

import java.util.HashMap;
import java.util.Map;
import com.ness.telegram.wishlistbot.model.Wish;
import org.springframework.stereotype.Service;

@Service
public class WishEditCacheServiceImpl extends WishAddCacheServiceImpl
        implements WishEditCacheService {

    private Map<Long, Wish> wishes;
    
    public WishEditCacheServiceImpl(){
        wishes = new HashMap();
    }

    @Override
    public void putWish(Long chatId, Wish wish) {
        wishes.put(chatId, wish);
    }

    @Override
    public Wish getWish(Long chatId) {
        return wishes.get(chatId);
    }
}