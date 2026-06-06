package com.example.nextoffer.career;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;

/**
 * Infers the ATS platform from a career page URL so users are not stuck on the wrong fetcher.
 */
public final class AtsTypeDetector {

    private AtsTypeDetector() {
    }

    public static Optional<AtsType> detect(String careerPageUrl) {
        if (careerPageUrl == null || careerPageUrl.isBlank()) {
            return Optional.empty();
        }
        try {
            URI uri = URI.create(careerPageUrl.trim());
            String host = uri.getHost() != null ? uri.getHost().toLowerCase(Locale.ROOT) : "";
            if (host.contains("boards.greenhouse.io") || host.contains("job-boards.greenhouse.io")) {
                return Optional.of(AtsType.GREENHOUSE);
            }
            if (host.contains("myworkdayjobs.com")) {
                return Optional.of(AtsType.WORKDAY);
            }
            if (host.contains("smartrecruiters.com")) {
                return Optional.of(AtsType.SMART_RECRUITERS);
            }
            if (host.contains("jobs.lever.co") || host.endsWith(".lever.co")) {
                return Optional.of(AtsType.LEVER);
            }
            return Optional.empty();
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public static AtsType resolve(AtsType requested, String careerPageUrl) {
        Optional<AtsType> detected = detect(careerPageUrl);
        if (detected.isPresent()) {
            return detected.get();
        }
        return requested != null ? requested : AtsType.GREENHOUSE;
    }
}
