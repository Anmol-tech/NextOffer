package com.example.nextoffer.career;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AtsTypeDetectorTest {

    @Test
    void detectsGreenhouseUrl() {
        assertEquals(
                AtsType.GREENHOUSE,
                AtsTypeDetector.detect("https://boards.greenhouse.io/stripe").orElseThrow());
    }

    @Test
    void detectsWorkdayUrl() {
        assertEquals(
                AtsType.WORKDAY,
                AtsTypeDetector.detect("https://nvidia.wd5.myworkdayjobs.com/en-US/NVIDIAExternalCareerSite").orElseThrow());
    }

    @Test
    void detectsSmartRecruitersUrl() {
        assertEquals(
                AtsType.SMART_RECRUITERS,
                AtsTypeDetector.detect("https://jobs.smartrecruiters.com/Visa").orElseThrow());
    }

    @Test
    void overridesWrongRequestedType() {
        assertEquals(
                AtsType.SMART_RECRUITERS,
                AtsTypeDetector.resolve(AtsType.GREENHOUSE, "https://jobs.smartrecruiters.com/Visa"));
    }
}
