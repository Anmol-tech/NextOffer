package com.example.nextoffer.resume;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

final class BulletOrderMerger {

    private BulletOrderMerger() {
    }

    static List<String> mergeOrder(List<String> preferred, List<String> original) {
        if (original.isEmpty()) {
            return List.copyOf(preferred);
        }
        if (preferred.isEmpty()) {
            return List.copyOf(original);
        }

        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        List<String> remaining = new ArrayList<>(original);

        for (String pref : preferred) {
            String match = findBestMatch(pref, remaining);
            if (match != null) {
                ordered.add(match);
                remaining.remove(match);
            }
        }

        ordered.addAll(remaining);
        return List.copyOf(ordered);
    }

    private static String findBestMatch(String preferred, List<String> candidates) {
        String normalizedPreferred = normalize(preferred);
        String best = null;
        int bestScore = 0;

        for (String candidate : candidates) {
            String normalizedCandidate = normalize(candidate);
            if (normalizedCandidate.equals(normalizedPreferred)) {
                return candidate;
            }
            int score = overlapScore(normalizedPreferred, normalizedCandidate);
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        return bestScore >= 3 ? best : null;
    }

    private static int overlapScore(String left, String right) {
        if (left.isBlank() || right.isBlank()) {
            return 0;
        }
        if (left.contains(right) || right.contains(left)) {
            return Math.max(left.length(), right.length());
        }

        String[] leftTokens = left.split("\\s+");
        int score = 0;
        for (String token : leftTokens) {
            if (token.length() >= 4 && right.contains(token)) {
                score++;
            }
        }
        return score;
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("\\\\[a-zA-Z@]+\\*?(\\[[^\\]]*\\])?\\{([^{}]*)\\}", "$2")
                .replaceAll("[{}]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
