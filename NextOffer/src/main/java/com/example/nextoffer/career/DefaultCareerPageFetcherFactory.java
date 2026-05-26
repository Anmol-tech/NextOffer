package com.example.nextoffer.career;


public class DefaultCareerPageFetcherFactory extends CareerPageFetcherFactory {

    @Override
    protected CareerPageFetcher createCustomFetcher(AtsType atsType) {
        return new CachingCareerPageProxy(new GreenhouseFetchStrategy());
    }
}
