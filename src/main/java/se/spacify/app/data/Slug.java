package se.spacify.app.data;

import java.util.Locale;

/** Turns a free-text name into a URL/Lua-identifier-safe slug for table and field addressing. */
public final class Slug {

    private Slug() {}

    public static String of(String name, String fallback) {
        if (name == null) {
            return fallback;
        }
        String slug = name.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
        return slug.isEmpty() ? fallback : slug;
    }
}
