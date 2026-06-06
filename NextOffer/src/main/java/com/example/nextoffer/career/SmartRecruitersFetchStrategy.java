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

public class SmartRecruitersFetchStrategy implements CareerPageFetchStrategy {

    private static final int PAGE_SIZE = 100;
    private static final int MAX_JOBS  = 500;

    private final HttpClient    httpClient;
    private final ObjectMapper  objectMapper;

    public SmartRecruitersFetchStrategy() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<JobPostingDto> fetch(String careerPageUrl) {
        String companyId = SmartRecruitersUrlResolver.parseFromUrl(careerPageUrl);
        return fetchAllPages(companyId, careerPageUrl);
    }

    @Override
    public List<JobPostingDto> fetchForWatch(CompanyWatch watch) {
        String companyId = SmartRecruitersUrlResolver.resolve(
                watch.getCareerPageUrl(), watch.getBoardToken());
        return fetchAllPages(companyId, watch.getCompanyName());
    }

    // -------------------------------------------------------------------------

    private List<JobPostingDto> fetchAllPages(String companyId, String companyName) {
        List<JobPostingDto> all = new ArrayList<>();
        int offset = 0;

        while (all.size() < MAX_JOBS) {
            SrPostingsResponse page = fetchPage(companyId, offset);
            if (page == null || page.content() == null || page.content().isEmpty()) {
                break;
            }
            for (SrPosting p : page.content()) {
                all.add(toDto(p, companyId, companyName));
            }
            int total = page.totalFound() != null ? page.totalFound() : 0;
            offset += PAGE_SIZE;
            if (offset >= total || all.size() >= MAX_JOBS) {
                break;
            }
        }
        return all;
    }

    private SrPostingsResponse fetchPage(String companyId, int offset) {
        String url = SmartRecruitersUrlResolver.apiUrl(companyId)
                + "?limit=" + PAGE_SIZE + "&offset=" + offset;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .header("User-Agent",
                        "Mozilla/5.0 (compatible; NextOffer-Bot/1.0)")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                throw new CareerPageFetchException("""
                        SmartRecruiters company "%s" was not found. Use the company careers URL \
                        (for example https://jobs.smartrecruiters.com/YourCompany), not a single job link, \
                        and set the platform to SmartRecruiters.""".formatted(companyId));
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new CareerPageFetchException(
                        "SmartRecruiters returned HTTP " + response.statusCode() + " for company \"" + companyId + "\".");
            }
            return objectMapper.readValue(response.body(), SrPostingsResponse.class);
        } catch (CareerPageFetchException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CareerPageFetchException(
                    "SmartRecruiters request failed for \"" + companyId + "\": " + e.getMessage(), e);
        }
    }

    private JobPostingDto toDto(SrPosting p, String companyId, String companyName) {
        String externalId = "sr-" + p.id();
        String applyUrl   = SmartRecruitersUrlResolver.applyUrl(companyId, p.id());
        String location   = p.fullLocation() != null ? p.fullLocation() : "Unspecified";
        String department = p.departmentLabel() != null ? p.departmentLabel() : "";

        return new JobPostingDto(
                externalId,
                companyName,
                p.name() != null ? p.name() : "Untitled",
                location,
                applyUrl,
                "",
                Instant.now(),
                department
        );
    }

    // ---- JSON response records ----

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SrPostingsResponse(
            @JsonProperty("totalFound") Integer totalFound,
            @JsonProperty("limit")      Integer limit,
            @JsonProperty("offset")     Integer offset,
            @JsonProperty("content")    List<SrPosting> content
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SrPosting(
            @JsonProperty("id")         String id,
            @JsonProperty("name")       String name,
            @JsonProperty("location")   SrLocation location,
            @JsonProperty("department") SrDepartment department,
            @JsonProperty("releasedDate") String releasedDate
    ) {
        String fullLocation() {
            return location != null ? location.fullLocation() : null;
        }
        String departmentLabel() {
            return department != null ? department.label() : null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SrLocation(
            @JsonProperty("fullLocation") String fullLocation
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SrDepartment(
            @JsonProperty("label") String label
    ) {}
}
