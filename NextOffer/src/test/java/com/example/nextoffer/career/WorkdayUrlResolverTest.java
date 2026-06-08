package com.example.nextoffer.career;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkdayUrlResolverTest {

    private static final String EXTERNAL_PATH =
            "/job/USA-WA-Seattle/Principal-Software-Development-Engineer_JR-0106537";

    @Test
    void buildsApplyUrlWithLocaleAndJobBoard() {
        var coords = WorkdayUrlResolver.parseFromUrl(
                "https://workday.wd5.myworkdayjobs.com/en-US/Workday");

        assertEquals("Workday", coords.jobBoard());
        assertEquals("en-US", coords.locale());
        assertEquals(
                "https://workday.wd5.myworkdayjobs.com/en-US/Workday" + EXTERNAL_PATH,
                coords.applyUrl(EXTERNAL_PATH));
    }

    @Test
    void buildsApplyUrlWithoutLocale() {
        var coords = WorkdayUrlResolver.parseFromUrl(
                "https://workday.wd5.myworkdayjobs.com/Workday");

        assertEquals(
                "https://workday.wd5.myworkdayjobs.com/Workday" + EXTERNAL_PATH,
                coords.applyUrl(EXTERNAL_PATH));
    }

    @Test
    void repairsLegacyApplyUrlMissingJobBoardPrefix() {
        String legacy = "https://workday.wd5.myworkdayjobs.com/job/USA-WA-Seattle/Engineer_JR-123";
        String careerPage = "https://workday.wd5.myworkdayjobs.com/en-US/Workday";

        assertEquals(
                "https://workday.wd5.myworkdayjobs.com/en-US/Workday/job/USA-WA-Seattle/Engineer_JR-123",
                WorkdayUrlResolver.repairApplyUrl(legacy, careerPage));
    }

    @Test
    void leavesCorrectApplyUrlUnchanged() {
        String correct =
                "https://workday.wd5.myworkdayjobs.com/en-US/Workday/job/USA-WA-Seattle/Engineer_JR-123";
        String careerPage = "https://workday.wd5.myworkdayjobs.com/en-US/Workday";

        assertEquals(correct, WorkdayUrlResolver.repairApplyUrl(correct, careerPage));
    }
}
