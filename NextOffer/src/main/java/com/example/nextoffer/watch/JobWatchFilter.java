package com.example.nextoffer.watch;

import com.example.nextoffer.job.JobPostingDto;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Applies optional location, keyword, and department filters configured on a company watch.
 */
public final class JobWatchFilter {

    private JobWatchFilter() {
    }

    public static boolean matches(CompanyWatch watch, JobPostingDto posting) {
        return matchesLocation(watch.getLocationFilter(), posting.location())
                && matchesKeywords(watch.getKeywordFilter(), posting.title(), posting.description())
                && matchesDepartment(watch.getDepartmentFilter(), posting.department(), posting.title(), posting.description());
    }

    public static List<JobPostingDto> apply(CompanyWatch watch, List<JobPostingDto> postings) {
        return postings.stream()
                .filter(posting -> matches(watch, posting))
                .toList();
    }

    static boolean matchesLocation(String filter, String location) {
        List<String> terms = parseTerms(filter);
        if (terms.isEmpty()) {
            return true;
        }
        String haystack = normalize(location);
        return terms.stream().anyMatch(term -> haystack.contains(term));
    }

    static boolean matchesKeywords(String filter, String title, String description) {
        List<String> terms = parseTerms(filter);
        if (terms.isEmpty()) {
            return true;
        }
        String haystack = normalize(title) + " " + normalize(description);
        return terms.stream().anyMatch(haystack::contains);
    }

    static boolean matchesDepartment(String filter, String department, String title, String description) {
        List<String> terms = parseTerms(filter);
        if (terms.isEmpty()) {
            return true;
        }
        String haystack = normalize(department) + " " + normalize(title) + " " + normalize(description);
        return terms.stream().anyMatch(haystack::contains);
    }

    static List<String> parseTerms(String filter) {
        if (filter == null || filter.isBlank()) {
            return List.of();
        }
        return Arrays.stream(filter.split("[,;\\n]+"))
                .map(String::trim)
                .filter(term -> !term.isBlank())
                .map(term -> term.toLowerCase(Locale.ROOT))
                .toList();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
