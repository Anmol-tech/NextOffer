package com.example.nextoffer.career;

import com.example.nextoffer.job.JobPostingDto;
import com.example.nextoffer.watch.CompanyWatch;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

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
        try {
            GreenhouseJobsResponse response = restClient.get()
                    .uri(API_TEMPLATE, boardToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
                        throw new CareerPageFetchException(greenhouseClientError(boardToken, clientResponse.getStatusCode().value()));
                    })
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
                            Instant.now(),
                            job.departmentNames()
                    ))
                    .toList();
        } catch (CareerPageFetchException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new CareerPageFetchException(greenhouseClientError(boardToken, 404), ex);
            }
            throw new CareerPageFetchException(
                    "Could not load Greenhouse jobs for board \"" + boardToken + "\": " + ex.getMessage(),
                    ex);
        }
    }

    private static String greenhouseClientError(String boardToken, int statusCode) {
        if (statusCode == 404) {
            return """
                    Greenhouse board "%s" was not found. Use the main career page URL \
                    (for example https://boards.greenhouse.io/stripe), not a single job link, \
                    and set the platform to Greenhouse.""".formatted(boardToken);
        }
        return "Greenhouse returned HTTP " + statusCode + " for board \"" + boardToken + "\".";
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
            GreenhouseLocation location,
            List<GreenhouseDepartment> departments
    ) {
        String locationName() {
            return location != null && location.name() != null ? location.name() : "Unspecified";
        }

        String departmentNames() {
            if (departments == null || departments.isEmpty()) {
                return "";
            }
            return departments.stream()
                    .map(GreenhouseDepartment::name)
                    .filter(name -> name != null && !name.isBlank())
                    .collect(Collectors.joining(", "));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GreenhouseLocation(String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GreenhouseDepartment(String name) {
    }
}
