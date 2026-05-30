package com.example.nextoffer.career;


public class DefaultCareerPageFetcherFactory extends CareerPageFetcherFactory {

    @Override
    protected CareerPageFetcher createCustomFetcher(AtsType atsType) {
        return switch (atsType) {
            case WORKDAY -> new WorkdayIntegrationFactory().createFetcher();
            default -> new CachingCareerPageProxy(new GreenhouseFetchStrategy());
        };
    }
}
