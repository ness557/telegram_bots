package com.ness.telegram.wishlistbot.service;

import java.util.List;
import com.ness.telegram.wishlistbot.model.Wish;

public interface WishDeleteCacheService {
    void putWishes(Long chatId, List<Wish> wishes);
    List<Wish> getWishes(Long chatId);
}