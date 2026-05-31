package com.example.nextoffer.career;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves the SmartRecruiters company identifier from a career page URL.
 *
 * Supported URL shapes:
 *   https://jobs.smartrecruiters.com/IKEA
 *   https://jobs.smartrecruiters.com/Visa/job-title-id
 *   https://IKEA.jobs.smartrecruiters.com        (branded subdomain)
 */
public class SmartRecruitersUrlResolver {

    private static final String API_BASE = "https://api.smartrecruiters.com/v1/companies";
    private static final String JOBS_BASE = "https://jobs.smartrecruiters.com";

    private static final Pattern SUBDOMAIN_PATTERN =
            Pattern.compile("^([a-zA-Z0-9_-]+)\\.jobs\\.smartrecruiters\\.com$");

    public static String resolve(String careerPageUrl, String boardToken) {
        if (boardToken != null && !boardToken.isBlank()) {
            return boardToken.trim();
        }
        return parseFromUrl(careerPageUrl);
    }

    public static String parseFromUrl(String careerPageUrl) {
        try {
            URI uri = URI.create(careerPageUrl.trim());
            String host = uri.getHost();

            // Branded subdomain: company.jobs.smartrecruiters.com
            Matcher m = SUBDOMAIN_PATTERN.matcher(host);
            if (m.matches()) {
                return m.group(1);
            }

            // Standard: jobs.smartrecruiters.com/CompanyIdentifier[/...]
            if (host.equalsIgnoreCase("jobs.smartrecruiters.com")) {
                String path = uri.getPath();
                if (path != null && !path.isBlank()) {
                    String[] segments = path.replaceAll("^/+", "").split("/");
                    if (segments.length > 0 && !segments[0].isBlank()) {
                        return segments[0];
                    }
                }
            }

            throw new IllegalArgumentException(
                    "Cannot extract SmartRecruiters company from URL: " + careerPageUrl);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid SmartRecruiters URL: " + careerPageUrl, e);
        }
    }

    public static String apiUrl(String companyIdentifier) {
        return API_BASE + "/" + companyIdentifier + "/postings";
    }

    public static String applyUrl(String companyIdentifier, String jobId) {
        return JOBS_BASE + "/" + companyIdentifier + "/" + jobId;
    }
}
