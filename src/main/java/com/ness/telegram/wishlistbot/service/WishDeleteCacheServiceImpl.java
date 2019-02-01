package com.ness.telegram.wishlistbot.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ness.telegram.wishlistbot.model.Wish;
import org.springframework.stereotype.Service;

@Service
public class WishDeleteCacheServiceImpl implements WishDeleteCacheService {

    private Map<Long, List<Wish>> wishes;

    public WishDeleteCacheServiceImpl() {
        wishes = new HashMap<>();
    }

    @Override
    public void putWishes(Long chatId, List<Wish> wishes) {
        this.wishes.put(chatId, wishes);
    }

    @Override
    public List<Wish> getWishes(Long chatId) {
        return this.wishes.get(chatId);
    }
}