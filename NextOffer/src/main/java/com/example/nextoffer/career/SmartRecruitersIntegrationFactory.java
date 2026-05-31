package com.example.nextoffer.career;

public class SmartRecruitersIntegrationFactory implements AtsIntegrationFactory {

    @Override
    public CareerPageFetcher createFetcher() {
        return new CachingCareerPageProxy(new SmartRecruitersFetchStrategy());
    }

    @Override
    public JobDescriptionParser createParser() {
        return json -> null;
    }

    @Override
    public JobPostingNormalizer createNormalizer() {
        return posting -> posting;
    }
}
