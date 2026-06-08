package com.example.nextoffer.watch;

import com.example.nextoffer.career.CareerPageFetchStrategy;
import com.example.nextoffer.career.CareerPageFetchStrategyFactory;
import com.example.nextoffer.job.JobPostingDto;
import com.example.nextoffer.watch.CompanyWatch;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyWatchControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CareerPageFetchStrategyFactory strategyFactory;

    private static final AtomicInteger JOB_COUNTER = new AtomicInteger();

    @BeforeEach
    void stubFetchStrategy() {
        JOB_COUNTER.set(0);
        when(strategyFactory.forAtsType(any())).thenReturn(new CareerPageFetchStrategy() {
            @Override
            public List<JobPostingDto> fetch(String careerPageUrl) {
                int n = JOB_COUNTER.incrementAndGet();
                return List.of(new JobPostingDto(
                        "stub-job-" + n,
                        "Stub Corp",
                        "Software Engineer " + n,
                        "Remote",
                        "https://example.com/jobs/" + n,
                        "Description " + n,
                        Instant.now()
                ));
            }
        });
    }

    @Test
    void pollAppliesLocationAndKeywordFilters() throws Exception {
        when(strategyFactory.forAtsType(any())).thenReturn(new CareerPageFetchStrategy() {
            @Override
            public List<JobPostingDto> fetch(String careerPageUrl) {
                return fetchForWatch(null);
            }

            @Override
            public List<JobPostingDto> fetchForWatch(CompanyWatch watch) {
                return List.of(
                        new JobPostingDto(
                                "gh-nyc",
                                "Stripe",
                                "Sales Representative",
                                "New York, NY",
                                "https://example.com/nyc",
                                "enterprise sales",
                                Instant.now(),
                                "Sales"
                        ),
                        new JobPostingDto(
                                "gh-sf",
                                "Stripe",
                                "Treasury Operations Specialist",
                                "San Francisco, CA",
                                "https://example.com/sf",
                                "treasury and payments",
                                Instant.now(),
                                "Operations"
                        )
                );
            }
        });

        String token = registerAndLogin();

        String createBody = """
                {
                  "companyName": "Stripe",
                  "careerPageUrl": "https://boards.greenhouse.io/stripe",
                  "atsType": "GREENHOUSE",
                  "locationFilter": "San Francisco",
                  "keywordFilter": "treasury"
                }
                """;

        String watchJson = mockMvc.perform(post("/api/watches")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.locationFilter").value("San Francisco"))
                .andExpect(jsonPath("$.keywordFilter").value("treasury"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number watchIdNumber = JsonPath.read(watchJson, "$.id");
        long watchId = watchIdNumber.longValue();

        mockMvc.perform(post("/api/watches/" + watchId + "/poll")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newJobsCount").value(1))
                .andExpect(jsonPath("$.newJobs[0].title").value("Treasury Operations Specialist"));
    }

    @Test
    void createWatchPollAndListJobs() throws Exception {
        String token = registerAndLogin();

        String createBody = """
                {
                  "companyName": "Stripe",
                  "careerPageUrl": "https://boards.greenhouse.io/stripe",
                  "atsType": "GREENHOUSE"
                }
                """;

        String watchJson = mockMvc.perform(post("/api/watches")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyName").value("Stripe"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number watchIdNumber = JsonPath.read(watchJson, "$.id");
        long watchId = watchIdNumber.longValue();

        mockMvc.perform(post("/api/watches/" + watchId + "/poll")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newJobsCount").value(1))
                .andExpect(jsonPath("$.newJobs[0].title").value("Software Engineer 1"));

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].companyName").value("Stub Corp"));

        mockMvc.perform(post("/api/watches/" + watchId + "/poll")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newJobsCount").value(1));

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private String registerAndLogin() throws Exception {
        String email = "watch-" + System.nanoTime() + "@example.com";
        String registerBody = """
                {
                  "email": "%s",
                  "password": "password123",
                  "fullName": "Watch Tester"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody)).andExpect(status().isCreated());

        String loginBody = """
                {
                  "email": "%s",
                  "password": "password123"
                }
                """.formatted(email);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(loginResponse, "$.token");
    }

    @Test
    void createWorkdayWatch() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(post("/api/watches")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Workday Corp",
                                  "careerPageUrl": "https://workday.wd5.myworkdayjobs.com/en-US/Workday",
                                  "atsType": "WORKDAY"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.atsType").value("WORKDAY"))
                .andExpect(jsonPath("$.companyName").value("Workday Corp"));
    }

    @Test
    void deleteWatchRemovesDiscoveredJobs() throws Exception {
        String token = registerAndLogin();

        String watchJson = mockMvc.perform(post("/api/watches")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Stub Corp",
                                  "careerPageUrl": "https://boards.greenhouse.io/stub",
                                  "atsType": "GREENHOUSE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number watchIdNumber = JsonPath.read(watchJson, "$.id");
        long watchId = watchIdNumber.longValue();

        mockMvc.perform(post("/api/watches/" + watchId + "/poll")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(delete("/api/watches/" + watchId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/watches")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
