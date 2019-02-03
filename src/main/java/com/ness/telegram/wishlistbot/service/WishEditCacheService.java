package com.ness.telegram.wishlistbot.service;

import com.ness.telegram.wishlistbot.model.Wish;

/**
 * WishEditCacheService
 */
public interface WishEditCacheService extends WishAddCacheService {
    void putWish(Long chatId, Wish wish);
    Wish getWish(Long chatId);
}