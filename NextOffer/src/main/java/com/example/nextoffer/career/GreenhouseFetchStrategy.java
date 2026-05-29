package com.example.nextoffer.career;

import com.example.nextoffer.job.JobPostingDto;
import com.example.nextoffer.watch.CompanyWatch;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

public class GreenhouseFetchStrategy implements CareerPageFetchStrategy {

    private static final String API_TEMPLATE = "https://boards-api.greenhouse.io/v1/boards/{boardToken}/jobs";

    private final RestClient restClient;

    public GreenhouseFetchStrategy() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(15_000);
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    @Override
    public List<JobPostingDto> fetch(String careerPageUrl) {
        throw new UnsupportedOperationException("Use fetchForWatch(CompanyWatch)");
    }

    @Override
    public List<JobPostingDto> fetchForWatch(CompanyWatch watch) {
        String boardToken = GreenhouseBoardTokenResolver.resolve(watch.getCareerPageUrl(), watch.getBoardToken());
        return fetchBoard(boardToken, watch.getCompanyName(), watch.getCareerPageUrl());
    }

    public List<JobPostingDto> fetchBoard(String boardToken, String companyName, String careerPageUrl) {
        GreenhouseJobsResponse response = restClient.get()
                .uri(API_TEMPLATE, boardToken)
                .retrieve()
                .body(GreenhouseJobsResponse.class);

        if (response == null || response.jobs() == null) {
            return List.of();
        }

        return response.jobs().stream()
                .map(job -> new JobPostingDto(
                        "gh-" + job.id(),
                        companyName,
                        job.title(),
                        job.locationName(),
                        job.absoluteUrl() != null ? job.absoluteUrl() : careerPageUrl,
                        job.content() != null ? job.content() : "",
                        Instant.now()
                ))
                .toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GreenhouseJobsResponse(List<GreenhouseJob> jobs) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GreenhouseJob(
            long id,
            String title,
            @JsonProperty("absolute_url") String absoluteUrl,
            String content,
            GreenhouseLocation location
    ) {
        String locationName() {
            return location != null && location.name() != null ? location.name() : "Unspecified";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GreenhouseLocation(String name) {
    }
}
