package com.ness.telegram.wishlistbot.service;

import java.util.List;

public interface WishDeleteCacheService {
    void putWishes(Long chatId, List<Long> wishIds);
    List<Long> getWishes(Long chatId);
}