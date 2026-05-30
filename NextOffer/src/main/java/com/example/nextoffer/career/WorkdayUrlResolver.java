package com.example.nextoffer.career;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a Workday career page URL to extract the tenant subdomain,
 * Workday instance (wd1/wd3/wd5/wd12...), and job-board path segment.
 *
 * Supported URL shapes:
 *   https://<tenant>.wd5.myworkdayjobs.com/en-US/<jobBoard>
 *   https://<tenant>.wd5.myworkdayjobs.com/<jobBoard>
 *   https://<tenant>.wd5.myworkdayjobs.com/en-US/<jobBoard>/jobs
 */
public class WorkdayUrlResolver {

    private static final Pattern HOST_PATTERN =
            Pattern.compile("^([a-zA-Z0-9_-]+)\\.(wd\\d+)\\.myworkdayjobs\\.com$");

    public record WorkdayCoordinates(String tenant, String instance, String jobBoard) {
        /** Base URL for this tenant, e.g. https://amazon.wd5.myworkdayjobs.com */
        public String baseUrl() {
            return "https://" + tenant + "." + instance + ".myworkdayjobs.com";
        }

        /** The Workday CXS jobs API endpoint for this board */
        public String cxsJobsUrl() {
            return baseUrl() + "/wday/cxs/" + tenant + "/" + jobBoard + "/jobs";
        }
    }

    public static WorkdayCoordinates resolve(String careerPageUrl, String boardToken) {
        if (boardToken != null && !boardToken.isBlank()) {
            // boardToken can store "tenant/instance/jobBoard" as a pre-parsed value
            String[] parts = boardToken.split("/", 3);
            if (parts.length == 3) {
                return new WorkdayCoordinates(parts[0], parts[1], parts[2]);
            }
        }
        return parseFromUrl(careerPageUrl);
    }

    public static WorkdayCoordinates parseFromUrl(String careerPageUrl) {
        try {
            URI uri = URI.create(careerPageUrl.trim());
            String host = uri.getHost();
            Matcher m = HOST_PATTERN.matcher(host);
            if (!m.matches()) {
                throw new IllegalArgumentException(
                        "Not a valid Workday URL (expected <tenant>.wd<N>.myworkdayjobs.com): " + careerPageUrl);
            }
            String tenant = m.group(1);
            String instance = m.group(2);
            String jobBoard = extractJobBoard(uri.getPath(), tenant);
            return new WorkdayCoordinates(tenant, instance, jobBoard);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse Workday URL: " + careerPageUrl, e);
        }
    }

    private static String extractJobBoard(String path, String tenant) {
        if (path == null || path.isBlank() || path.equals("/")) {
            return tenant + "_careers";
        }
        // Strip leading slash, split segments
        String[] segments = path.replaceAll("^/+", "").split("/");
        // Skip locale segments like "en-US", "en_US"
        for (String segment : segments) {
            if (segment.isBlank()) continue;
            if (segment.matches("[a-z]{2}[-_][A-Z]{2}")) continue; // e.g. en-US
            if (segment.equalsIgnoreCase("jobs")) continue;
            if (segment.equalsIgnoreCase("job")) continue;
            return segment;
        }
        return tenant + "_careers";
    }
}
