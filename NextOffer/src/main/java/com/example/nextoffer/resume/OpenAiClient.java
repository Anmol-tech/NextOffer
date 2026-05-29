package com.example.nextoffer.resume;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

    private enum Provider {
        OPENROUTER,
        OPENAI
    }

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Provider provider;
    private final String model;
    private final boolean enabled;

    public OpenAiClient(
            ObjectMapper objectMapper,
            @Value("${app.llm.provider:auto}") String providerSetting,
            @Value("${app.openrouter.api-key:}") String openRouterApiKey,
            @Value("${app.openrouter.model:nvidia/nemotron-3-nano-30b-a3b:free}") String openRouterModel,
            @Value("${app.openrouter.base-url:https://openrouter.ai/api}") String openRouterBaseUrl,
            @Value("${app.openai.api-key:}") String openAiApiKey,
            @Value("${app.openai.model:gpt-4o-mini}") String openAiModel) {
        this.objectMapper = objectMapper;

        String normalizedOpenRouterKey = normalizeKey(openRouterApiKey);
        String normalizedOpenAiKey = normalizeKey(openAiApiKey);
        String normalizedProvider = providerSetting == null ? "auto" : providerSetting.trim().toLowerCase(Locale.ROOT);

        Provider resolvedProvider;
        String apiKey;
        String baseUrl;
        String resolvedModel;

        switch (normalizedProvider) {
            case "openrouter" -> {
                resolvedProvider = Provider.OPENROUTER;
                apiKey = normalizedOpenRouterKey;
                baseUrl = openRouterBaseUrl;
                resolvedModel = openRouterModel;
            }
            case "openai" -> {
                resolvedProvider = Provider.OPENAI;
                apiKey = normalizedOpenAiKey;
                baseUrl = "https://api.openai.com";
                resolvedModel = openAiModel;
            }
            default -> {
                if (!normalizedOpenRouterKey.isBlank()) {
                    resolvedProvider = Provider.OPENROUTER;
                    apiKey = normalizedOpenRouterKey;
                    baseUrl = openRouterBaseUrl;
                    resolvedModel = openRouterModel;
                } else if (!normalizedOpenAiKey.isBlank()) {
                    resolvedProvider = Provider.OPENAI;
                    apiKey = normalizedOpenAiKey;
                    baseUrl = "https://api.openai.com";
                    resolvedModel = openAiModel;
                } else {
                    resolvedProvider = Provider.OPENROUTER;
                    apiKey = "";
                    baseUrl = openRouterBaseUrl;
                    resolvedModel = openRouterModel;
                }
            }
        }

        this.provider = resolvedProvider;
        this.model = resolvedModel;
        this.enabled = !apiKey.isBlank();

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey);

        if (resolvedProvider == Provider.OPENROUTER) {
            builder.defaultHeader("HTTP-Referer", "http://localhost:8080")
                    .defaultHeader("X-Title", "NextOffer");
        }

        this.restClient = builder.build();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String providerName() {
        return provider.name().toLowerCase(Locale.ROOT);
    }

    public TailoredResumeContent tailor(String jobTitle, String companyName, String jobDescription, TailoredResumeContent base) {
        if (!enabled) {
            throw new IllegalStateException("No LLM API key configured (set OPENROUTER_API_KEY or OPENAI_API_KEY)");
        }

        String prompt = """
                You tailor a resume for a specific job. Rules:
                - NEVER invent employers, degrees, dates, or projects not present in the base resume.
                - Only rewrite, reorder, and emphasize existing facts.
                - Return JSON only with keys: summary (string), skills (string array), experienceBullets (string array).
                - experienceBullets must be the same count or fewer than the base resume bullets.
                - Each bullet must remain factually grounded in the base resume.
                - Do not include markdown fences or explanation text outside the JSON object.

                Job title: %s
                Company: %s
                Job description:
                %s

                Base resume JSON:
                %s
                """.formatted(
                jobTitle,
                companyName,
                truncate(jobDescription, 4000),
                toJson(base)
        );

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", "You are a resume tailoring assistant. Output valid JSON only."),
                    Map.of("role", "user", "content", prompt)
            ));

            if (provider == Provider.OPENAI) {
                body.put("response_format", Map.of("type", "json_object"));
            }

            String response = restClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            JsonNode parsed = objectMapper.readTree(extractJson(content));

            return new TailoredResumeContent(
                    parsed.path("summary").asText(base.summary()),
                    readArray(parsed.path("skills"), base.skills()),
                    readArray(parsed.path("experienceBullets"), base.experienceBullets())
            );
        } catch (Exception ex) {
            log.warn("{} tailoring failed: {}", provider.name(), ex.getMessage());
            throw new ResumeGenerationException(provider.name() + " tailoring failed: " + ex.getMessage(), ex);
        }
    }

    public String reviewLatex(String latex, String compileLog) {
        if (!enabled) {
            throw new IllegalStateException("No LLM API key configured (set OPENROUTER_API_KEY or OPENAI_API_KEY)");
        }

        String prompt = """
                Review this LaTeX resume and return the corrected full document.
                Rules:
                - Fix syntax, unbalanced environments, and compile errors only.
                - Do NOT invent employers, degrees, projects, dates, or skills.
                - Preserve the preamble, margins, spacing, section layout, and formatting commands.
                - Keep \\documentclass{resume} and existing package/usepackage lines unchanged unless required to compile.
                - Return ONLY the complete LaTeX source. No markdown fences or explanation.

                Compile errors (if any):
                %s

                LaTeX document:
                %s
                """.formatted(
                compileLog == null || compileLog.isBlank() ? "none reported" : truncate(compileLog, 3000),
                truncate(latex, 12000)
        );

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", "You are a LaTeX resume reviewer. Output corrected LaTeX only."),
                    Map.of("role", "user", "content", prompt)
            ));

            String response = restClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            return extractLatexDocument(content);
        } catch (Exception ex) {
            log.warn("{} LaTeX review failed: {}", provider.name(), ex.getMessage());
            throw new ResumeGenerationException(provider.name() + " LaTeX review failed: " + ex.getMessage(), ex);
        }
    }

    private static String extractLatexDocument(String content) {
        if (content == null || content.isBlank()) {
            throw new ResumeGenerationException("LLM returned empty LaTeX review");
        }

        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int closingFence = trimmed.lastIndexOf("```");
            if (firstNewline >= 0 && closingFence > firstNewline) {
                trimmed = trimmed.substring(firstNewline + 1, closingFence).trim();
            }
        }

        if (!trimmed.contains("\\begin{document}")) {
            throw new ResumeGenerationException("LLM review did not return a complete LaTeX document");
        }

        return trimmed;
    }

    private static String normalizeKey(String apiKey) {
        return apiKey == null ? "" : apiKey.trim();
    }

    private static String extractJson(String content) {
        if (content == null || content.isBlank()) {
            throw new ResumeGenerationException("LLM returned empty content");
        }

        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int closingFence = trimmed.lastIndexOf("```");
            if (firstNewline >= 0 && closingFence > firstNewline) {
                trimmed = trimmed.substring(firstNewline + 1, closingFence).trim();
            }
        }

        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }

        return trimmed;
    }

    private String toJson(TailoredResumeContent content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (Exception ex) {
            throw new ResumeGenerationException("Failed to serialize resume content", ex);
        }
    }

    private List<String> readArray(JsonNode node, List<String> fallback) {
        if (!node.isArray()) {
            return fallback;
        }
        List<String> values = new java.util.ArrayList<>();
        node.forEach(item -> {
            if (item.isTextual() && !item.asText().isBlank()) {
                values.add(item.asText().trim());
            }
        });
        return values.isEmpty() ? fallback : values;
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        String cleaned = text.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        return cleaned.length() <= max ? cleaned : cleaned.substring(0, max);
    }
}
