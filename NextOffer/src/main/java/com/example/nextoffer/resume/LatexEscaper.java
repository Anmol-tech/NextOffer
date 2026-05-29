package com.example.nextoffer.resume;

import java.util.List;

final class LatexEscaper {

    private LatexEscaper() {
    }

    static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\textbackslash{}")
                .replace("&", "\\&")
                .replace("%", "\\%")
                .replace("$", "\\$")
                .replace("#", "\\#")
                .replace("_", "\\_")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("~", "\\textasciitilde{}")
                .replace("^", "\\textasciicircum{}");
    }

    static String toItemize(List<String> items) {
        if (items.isEmpty()) {
            return "\\item None listed\n";
        }
        StringBuilder builder = new StringBuilder();
        for (String item : items) {
            builder.append("\\item ").append(escape(item)).append('\n');
        }
        return builder.toString();
    }
}
