package com.example.nextoffer.career;


/**
 * Factory Method — subclasses (or this factory) decide which fetcher to instantiate.
 */
public abstract class CareerPageFetcherFactory {

    public final CareerPageFetcher createFetcher(AtsType atsType) {
        return switch (atsType) {
            case GREENHOUSE -> new GreenhouseIntegrationFactory().createFetcher();
            case LEVER, CUSTOM_HTML -> createCustomFetcher(atsType);
        };
    }

    protected abstract CareerPageFetcher createCustomFetcher(AtsType atsType);
}
