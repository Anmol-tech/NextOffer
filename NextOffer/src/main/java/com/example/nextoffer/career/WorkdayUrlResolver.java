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

    public record WorkdayCoordinates(String tenant, String instance, String jobBoard, String locale) {
        /** Base URL for this tenant, e.g. https://amazon.wd5.myworkdayjobs.com */
        public String baseUrl() {
            return "https://" + tenant + "." + instance + ".myworkdayjobs.com";
        }

        /** The Workday CXS jobs API endpoint for this board */
        public String cxsJobsUrl() {
            return baseUrl() + "/wday/cxs/" + tenant + "/" + jobBoard + "/jobs";
        }

        /** Public career-page path prefix, e.g. /en-US/Workday or /Workday */
        public String jobBoardBasePath() {
            if (locale != null && !locale.isBlank()) {
                return "/" + locale + "/" + jobBoard;
            }
            return "/" + jobBoard;
        }

        /** Build a browser apply URL from the API externalPath (e.g. /job/Location/Title_ID). */
        public String applyUrl(String externalPath) {
            if (externalPath == null || externalPath.isBlank()) {
                return baseUrl() + jobBoardBasePath();
            }
            String path = externalPath.startsWith("/") ? externalPath : "/" + externalPath;
            return baseUrl() + jobBoardBasePath() + path;
        }
    }

    /**
     * Fix legacy apply URLs stored without the job-board prefix
     * (e.g. .../job/Location/Title vs .../en-US/Workday/job/Location/Title).
     */
    public static String repairApplyUrl(String applyUrl, String careerPageUrl) {
        if (applyUrl == null || applyUrl.isBlank() || careerPageUrl == null || careerPageUrl.isBlank()) {
            return applyUrl;
        }
        try {
            String path = URI.create(applyUrl.trim()).getPath();
            if (path == null || !path.startsWith("/job/")) {
                return applyUrl;
            }
            return parseFromUrl(careerPageUrl).applyUrl(path);
        } catch (Exception e) {
            return applyUrl;
        }
    }

    public static WorkdayCoordinates resolve(String careerPageUrl, String boardToken) {
        if (boardToken != null && !boardToken.isBlank()) {
            // boardToken can store "tenant/instance/jobBoard" as a pre-parsed value
            String[] parts = boardToken.split("/", 3);
            if (parts.length == 3) {
                return new WorkdayCoordinates(parts[0], parts[1], parts[2], null);
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
            PathInfo pathInfo = extractPathInfo(uri.getPath(), tenant);
            return new WorkdayCoordinates(tenant, instance, pathInfo.jobBoard(), pathInfo.locale());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse Workday URL: " + careerPageUrl, e);
        }
    }

    private record PathInfo(String locale, String jobBoard) {}

    private static PathInfo extractPathInfo(String path, String tenant) {
        String defaultBoard = tenant + "_careers";
        if (path == null || path.isBlank() || path.equals("/")) {
            return new PathInfo(null, defaultBoard);
        }
        String locale = null;
        String jobBoard = defaultBoard;
        String[] segments = path.replaceAll("^/+", "").split("/");
        for (String segment : segments) {
            if (segment.isBlank()) continue;
            if (segment.matches("[a-z]{2}[-_][A-Z]{2}")) {
                locale = segment.replace('_', '-');
                continue;
            }
            if (segment.equalsIgnoreCase("jobs") || segment.equalsIgnoreCase("job")) continue;
            jobBoard = segment;
            break;
        }
        return new PathInfo(locale, jobBoard);
    }
}
