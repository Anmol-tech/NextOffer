package com.example.nextoffer.career;

import com.example.nextoffer.job.JobPostingDto;
import com.example.nextoffer.watch.CompanyWatch;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class WorkdayFetchStrategy implements CareerPageFetchStrategy {

    private static final int PAGE_SIZE = 20;
    private static final int MAX_JOBS = 300;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WorkdayFetchStrategy() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<JobPostingDto> fetch(String careerPageUrl) {
        WorkdayUrlResolver.WorkdayCoordinates coords =
                WorkdayUrlResolver.parseFromUrl(careerPageUrl);
        return fetchAllPages(coords, extractCompanyName(careerPageUrl));
    }

    @Override
    public List<JobPostingDto> fetchForWatch(CompanyWatch watch) {
        WorkdayUrlResolver.WorkdayCoordinates coords =
                WorkdayUrlResolver.resolve(watch.getCareerPageUrl(), watch.getBoardToken());
        return fetchAllPages(coords, watch.getCompanyName());
    }

    private List<JobPostingDto> fetchAllPages(
            WorkdayUrlResolver.WorkdayCoordinates coords,
            String companyName) {

        List<JobPostingDto> all = new ArrayList<>();
        int offset = 0;

        while (all.size() < MAX_JOBS) {
            WorkdayJobsResponse page = fetchPage(coords.cxsJobsUrl(), coords.baseUrl(), offset);
            if (page == null || page.jobPostings() == null || page.jobPostings().isEmpty()) {
                break;
            }
            for (WorkdayJobPosting posting : page.jobPostings()) {
                all.add(toDto(posting, coords, companyName));
            }
            int total = page.total() != null ? page.total() : 0;
            offset += PAGE_SIZE;
            if (offset >= total || all.size() >= MAX_JOBS) {
                break;
            }
        }
        return all;
    }

    private WorkdayJobsResponse fetchPage(String cxsUrl, String baseUrl, int offset) {
        String bodyJson = String.format(
                "{\"limit\":%d,\"offset\":%d,\"searchText\":\"\",\"locations\":[]}",
                PAGE_SIZE, offset);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(cxsUrl))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Referer", baseUrl + "/")
                .header("Origin", baseUrl)
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("HTTP " + response.statusCode() + " from Workday API: " + cxsUrl);
            }
            return objectMapper.readValue(response.body(), WorkdayJobsResponse.class);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Workday API request failed for " + cxsUrl + ": " + e.getMessage(), e);
        }
    }

    private JobPostingDto toDto(
            WorkdayJobPosting posting,
            WorkdayUrlResolver.WorkdayCoordinates coords,
            String companyName) {

        // Use bulletFields[0] (e.g. "JR343059") as unique ID if jobPostingId absent
        String rawId = posting.jobPostingId();
        if ((rawId == null || rawId.isBlank()) && posting.bulletFields() != null
                && !posting.bulletFields().isEmpty()) {
            rawId = posting.bulletFields().get(0);
        }
        String externalId = "wd-" + (rawId != null
                ? rawId.replaceAll("[^a-zA-Z0-9_-]", "_")
                : posting.title().replaceAll("\\s+", "_") + "_" + System.nanoTime());

        String applyUrl = posting.externalPath() != null
                ? coords.baseUrl() + posting.externalPath()
                : coords.baseUrl();

        String location = posting.locationsText() != null ? posting.locationsText() : "Unspecified";

        return new JobPostingDto(
                externalId,
                companyName,
                posting.title() != null ? posting.title() : "Untitled",
                location,
                applyUrl,
                "",
                Instant.now(),
                posting.jobFamilyGroup()
        );
    }

    private static String extractCompanyName(String url) {
        try {
            return URI.create(url).getHost().split("\\.")[0];
        } catch (Exception e) {
            return "Unknown";
        }
    }

    // ---- JSON response records ----

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WorkdayJobsResponse(
            @JsonProperty("jobPostings") List<WorkdayJobPosting> jobPostings,
            @JsonProperty("total") Integer total
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WorkdayJobPosting(
            @JsonProperty("title") String title,
            @JsonProperty("externalPath") String externalPath,
            @JsonProperty("locationsText") String locationsText,
            @JsonProperty("postedOn") String postedOn,
            @JsonProperty("jobPostingId") String jobPostingId,
            @JsonProperty("jobFamilyGroup") String jobFamilyGroup,
            @JsonProperty("bulletFields") List<String> bulletFields
    ) {}
}
