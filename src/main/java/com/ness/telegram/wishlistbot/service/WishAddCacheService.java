package com.ness.telegram.wishlistbot.service;

public interface WishAddCacheService {
    void putLabel(Long key, String value);
    void putLink(Long key, String value);
    String getLabel(Long key);
    String getLink(Long key);
}