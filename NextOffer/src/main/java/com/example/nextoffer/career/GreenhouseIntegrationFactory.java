package com.example.nextoffer.career;


public class GreenhouseIntegrationFactory implements AtsIntegrationFactory {

    @Override
    public CareerPageFetcher createFetcher() {
        return new CachingCareerPageProxy(new GreenhouseFetchStrategy());
    }

    @Override
    public JobDescriptionParser createParser() {
        return GreenhouseJobAdapter::fromJson;
    }

    @Override
    public JobPostingNormalizer createNormalizer() {
        return posting -> posting;
    }
}
