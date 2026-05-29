package com.example.nextoffer.watch;

import com.example.nextoffer.career.AtsType;
import com.example.nextoffer.job.JobPostingDto;
import com.example.nextoffer.user.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobWatchFilterTest {

    @Test
    void passesAllWhenFiltersEmpty() {
        CompanyWatch watch = watchWithFilters(null, null, null);
        JobPostingDto posting = posting("Remote", "Engineer", "Engineering", "Build APIs");

        assertTrue(JobWatchFilter.matches(watch, posting));
    }

    @Test
    void matchesLocationFilter() {
        CompanyWatch watch = watchWithFilters("San Francisco, Remote", null, null);

        assertTrue(JobWatchFilter.matches(watch, posting("San Francisco, CA", "Engineer", "", "")));
        assertTrue(JobWatchFilter.matches(watch, posting("Remote - US", "Engineer", "", "")));
        assertFalse(JobWatchFilter.matches(watch, posting("New York, NY", "Engineer", "", "")));
    }

    @Test
    void matchesKeywordFilter() {
        CompanyWatch watch = watchWithFilters(null, "treasury, payments", null);

        assertTrue(JobWatchFilter.matches(watch, posting("NYC", "Treasury Operations Specialist", "", "")));
        assertTrue(JobWatchFilter.matches(watch, posting("NYC", "Analyst", "", "payments processing experience")));
        assertFalse(JobWatchFilter.matches(watch, posting("NYC", "Software Engineer", "", "backend APIs")));
    }

    @Test
    void matchesDepartmentFilter() {
        CompanyWatch watch = watchWithFilters(null, null, "engineering, product");

        assertTrue(JobWatchFilter.matches(watch, posting("NYC", "Engineer", "Engineering", "")));
        assertTrue(JobWatchFilter.matches(watch, posting("NYC", "Product Manager", "", "")));
        assertFalse(JobWatchFilter.matches(watch, posting("NYC", "Sales Rep", "Sales", "")));
    }

    private static CompanyWatch watchWithFilters(String location, String keywords, String department) {
        CompanyWatch watch = new CompanyWatch(
                new User("u@example.com", "hash", "User"),
                "Acme",
                "https://boards.greenhouse.io/acme",
                null,
                AtsType.GREENHOUSE);
        watch.setLocationFilter(location);
        watch.setKeywordFilter(keywords);
        watch.setDepartmentFilter(department);
        return watch;
    }

    private static JobPostingDto posting(String location, String title, String department, String description) {
        return new JobPostingDto(
                "gh-1",
                "Acme",
                title,
                location,
                "https://example.com/apply",
                description,
                Instant.now(),
                department);
    }
}
