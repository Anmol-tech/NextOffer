package com.example.nextoffer.career;

import com.example.nextoffer.job.JobPostingDto;

import com.example.nextoffer.job.JobPostingDto;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy — caches fetch results to reduce load on career sites.
 */
public class CachingCareerPageProxy implements CareerPageFetcher {

    private final CareerPageFetchStrategy strategy;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Duration ttl = Duration.ofMinutes(15);

    public CachingCareerPageProxy(CareerPageFetchStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public List<JobPostingDto> fetchOpenings(String careerPageUrl) {
        CacheEntry entry = cache.get(careerPageUrl);
        if (entry != null && entry.expiresAt().isAfter(Instant.now())) {
            return entry.jobs();
        }
        List<JobPostingDto> fresh = strategy.fetch(careerPageUrl);
        cache.put(careerPageUrl, new CacheEntry(fresh, Instant.now().plus(ttl)));
        return fresh;
    }

    private record CacheEntry(List<JobPostingDto> jobs, Instant expiresAt) {
    }
}
