package com.example.nextoffer.career;


/**
 * Abstract Factory — produces a family of ATS-specific integration components.
 */
public interface AtsIntegrationFactory {

    CareerPageFetcher createFetcher();

    JobDescriptionParser createParser();

    JobPostingNormalizer createNormalizer();
}
