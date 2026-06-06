package com.example.nextoffer.tracker;

import com.example.nextoffer.career.CareerPageFetchStrategy;
import com.example.nextoffer.career.CareerPageFetchStrategyFactory;
import com.example.nextoffer.job.JobPostingDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationTrackerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CareerPageFetchStrategyFactory strategyFactory;

    @BeforeEach
    void stubFetch() {
        when(strategyFactory.forAtsType(any())).thenReturn(new CareerPageFetchStrategy() {
            @Override
            public List<JobPostingDto> fetch(String careerPageUrl) {
                return List.of(new JobPostingDto(
                        "tracker-job-1",
                        "Tracker Co",
                        "Backend Intern",
                        "Remote",
                        "https://example.com/apply",
                        "Java and Spring experience.",
                        Instant.now()
                ));
            }
        });
    }

    @Test
    void updateApplicationStatusAndListJobs() throws Exception {
        String token = registerAndLogin();

        String watchJson = mockMvc.perform(post("/api/watches")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Tracker Co",
                                  "careerPageUrl": "https://boards.greenhouse.io/tracker",
                                  "atsType": "GREENHOUSE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long watchId = objectMapper.readTree(watchJson).get("id").asLong();

        mockMvc.perform(post("/api/watches/" + watchId + "/poll")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String jobsJson = mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationStatus").value("NEW"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long jobId = objectMapper.readTree(jobsJson).get(0).get("id").asLong();

        mockMvc.perform(patch("/api/jobs/" + jobId + "/application-status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"APPLIED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationStatus").value("APPLIED"));

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationStatus").value("APPLIED"));
    }

    private String registerAndLogin() throws Exception {
        String email = "tracker-" + System.nanoTime() + "@example.com";
        String registerBody = """
                {"email":"%s","password":"password123","fullName":"Tracker Tester"}
                """.formatted(email);

        String authJson = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(authJson).get("token").asText();
    }
}
