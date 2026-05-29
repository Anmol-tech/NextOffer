package com.example.nextoffer.resume;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResumeControllerTest {

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
                        "resume-job-1",
                        "Test Co",
                        "Software Engineer Intern",
                        "Remote",
                        "https://example.com/apply",
                        "Looking for Java, React, SQL, REST API experience.",
                        Instant.now()
                ));
            }
        });
    }

    @Test
    void saveBaseAndGenerateTailoredResume() throws Exception {
        String email = "resume-" + System.nanoTime() + "@example.com";
        String registerBody = """
                {"email":"%s","password":"password123","fullName":"Resume Tester"}
                """.formatted(email);

        String authJson = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(authJson).get("token").asText();

        String baseResume = """
                {
                  "rawText": "Software engineering student with Java and React experience.\\nSkills: Java, React, SQL, Git\\n- Built a campus job board with Spring Boot\\n- Implemented REST APIs with PostgreSQL\\n- Led a team project using React and TypeScript"
                }
                """;

        mockMvc.perform(put("/api/resumes/base")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(baseResume))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rawText").isNotEmpty());

        String watchBody = """
                {"companyName":"Test Co","careerPageUrl":"https://boards.greenhouse.io/stripe","atsType":"GREENHOUSE"}
                """;

        String watchJson = mockMvc.perform(post("/api/watches")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(watchBody))
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
                .andReturn()
                .getResponse()
                .getContentAsString();

        long jobId = objectMapper.readTree(jobsJson).get(0).get("id").asLong();

        mockMvc.perform(post("/api/jobs/" + jobId + "/resumes/generate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobPostingId").value(jobId))
                .andExpect(jsonPath("$.content.summary").isNotEmpty());
    }
}
