package com.example.nextoffer.career;


public class DefaultCareerPageFetcherFactory extends CareerPageFetcherFactory {

    @Override
    protected CareerPageFetcher createCustomFetcher(AtsType atsType) {
        return switch (atsType) {
            case WORKDAY -> new WorkdayIntegrationFactory().createFetcher();
            case SMART_RECRUITERS -> new SmartRecruitersIntegrationFactory().createFetcher();
            default -> new CachingCareerPageProxy(new GreenhouseFetchStrategy());
        };
    }
}
