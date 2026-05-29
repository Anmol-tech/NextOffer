package com.example.nextoffer.career;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GreenhouseBoardTokenResolver {

    private static final Pattern BOARDS_PATH = Pattern.compile("boards\\.greenhouse\\.io/([^/?#]+)");
    private static final Pattern JOB_BOARDS_PATH = Pattern.compile("job-boards\\.greenhouse\\.io/([^/?#]+)");

    private GreenhouseBoardTokenResolver() {
    }

    public static String resolve(String careerPageUrl, String explicitBoardToken) {
        if (explicitBoardToken != null && !explicitBoardToken.isBlank()) {
            return explicitBoardToken.trim();
        }
        if (careerPageUrl == null || careerPageUrl.isBlank()) {
            throw new IllegalArgumentException("Career page URL or board token is required for Greenhouse");
        }
        String url = careerPageUrl.trim();
        Matcher boards = BOARDS_PATH.matcher(url);
        if (boards.find()) {
            return boards.group(1);
        }
        Matcher jobBoards = JOB_BOARDS_PATH.matcher(url);
        if (jobBoards.find()) {
            return jobBoards.group(1);
        }
        try {
            URI uri = URI.create(url);
            String path = uri.getPath();
            if (path != null && path.length() > 1) {
                String segment = path.startsWith("/") ? path.substring(1) : path;
                int slash = segment.indexOf('/');
                return slash > 0 ? segment.substring(0, slash) : segment;
            }
        } catch (Exception ignored) {
            // fall through
        }
        throw new IllegalArgumentException(
                "Could not determine Greenhouse board token from URL. Set boardToken explicitly.");
    }
}
