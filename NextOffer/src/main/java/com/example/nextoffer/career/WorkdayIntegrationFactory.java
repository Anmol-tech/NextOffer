package com.example.nextoffer.career;

public class WorkdayIntegrationFactory implements AtsIntegrationFactory {

    @Override
    public CareerPageFetcher createFetcher() {
        return new CachingCareerPageProxy(new WorkdayFetchStrategy());
    }

    @Override
    public JobDescriptionParser createParser() {
        return json -> null; // Workday public API doesn't return full JD HTML
    }

    @Override
    public JobPostingNormalizer createNormalizer() {
        return posting -> posting;
    }
}
