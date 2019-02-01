package com.ness.telegram.wishlistbot.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class WishAddCacheServiceImpl implements WishAddCacheService {

    private Map<Long, String> names;
    private Map<Long, String> links;

    public WishAddCacheServiceImpl(){
        names = new HashMap<>();
        links = new HashMap<>();
    }

    @Override
    public void putLabel(Long key, String value) {
        names.put(key, value);
    }

    @Override
    public void putLink(Long key, String value) {
        links.put(key, value);
    }

    @Override
    public String getLabel(Long key) {
        return names.get(key);
    }

    @Override
    public String getLink(Long key) {
        return links.get(key);
    }  
}