package com.example.nextoffer.career;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompanyBrandingFlyweightFactory {

    private final Map<String, CompanyBrandingFlyweight> cache = new ConcurrentHashMap<>();

    public CompanyBrandingFlyweight get(String companyKey, String displayName, String logoUrl) {
        return cache.computeIfAbsent(companyKey, key -> new CompanyBrandingFlyweight(key, displayName, logoUrl));
    }
}
