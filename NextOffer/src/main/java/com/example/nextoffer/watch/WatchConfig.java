package com.example.nextoffer.watch;

import com.example.nextoffer.career.CareerPageFetchStrategyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WatchConfig {

    @Bean
    CompanyWatchSubject companyWatchSubject() {
        return new CompanyWatchSubject();
    }

    @Bean
    JobIngestionMediator jobIngestionMediator(
            CareerPageFetchStrategyFactory strategyFactory,
            CompanyWatchSubject companyWatchSubject) {
        return new JobIngestionMediator(strategyFactory, companyWatchSubject);
    }
}
