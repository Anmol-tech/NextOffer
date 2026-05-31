package com.example.nextoffer.career;

import org.springframework.stereotype.Component;

@Component
public class CareerPageFetchStrategyFactory {

    public CareerPageFetchStrategy forAtsType(AtsType atsType) {
        return switch (atsType) {
            case GREENHOUSE -> new GreenhouseFetchStrategy();
            case WORKDAY -> new WorkdayFetchStrategy();
            case SMART_RECRUITERS -> new SmartRecruitersFetchStrategy();
            case LEVER, CUSTOM_HTML -> new GreenhouseFetchStrategy();
        };
    }
}
