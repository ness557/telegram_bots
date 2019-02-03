package com.ness.telegram.wishlistbot.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WishDeleteCacheServiceImpl implements WishDeleteCacheService {

    private Map<Long, List<Long>> wishIds;

    public WishDeleteCacheServiceImpl() {
        wishIds = new HashMap<>();
    }

    @Override
    public void putWishes(Long chatId, List<Long> wishIds) {
        this.wishIds.put(chatId, wishIds);
    }

    @Override
    public List<Long> getWishes(Long chatId) {
        return this.wishIds.get(chatId);
    }
}