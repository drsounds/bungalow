package se.spacify.app.data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Display formatting for {@code se.spacify.app.data} views: XML-escaping (the Spider
 * {@code ${...}} interpolation splices values into markup with no escaping of its own,
 * so any free-text value we hand the template must be escaped here first) and the
 * per-{@link se.spacify.app.data.model.FieldType} pretty-printing the app's spec calls for.
 */
public final class Format {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final long DAY_MS = 24L * 60 * 60 * 1000;
    private static final long AGO_THRESHOLD_MS = 14 * DAY_MS;

    private Format() {}

    /** Escape {@code s} for safe use as XML text or attribute content. Never returns null. */
    public static String xml(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&'  -> out.append("&amp;");
                case '<'  -> out.append("&lt;");
                case '>'  -> out.append("&gt;");
                case '"'  -> out.append("&quot;");
                case '\'' -> out.append("&apos;");
                default   -> out.append(c);
            }
        }
        return out.toString();
    }

    /** Whole number with grouping separators, e.g. {@code 1,234,567}. */
    public static String number(long v) {
        return String.format(Locale.US, "%,d", v);
    }

    /** Decimal number with grouping and two decimals, e.g. {@code 1,234.50}. */
    public static String decimal(double v) {
        return String.format(Locale.US, "%,.2f", v);
    }

    /**
     * A timestamp as "N units ago" when under {@link #AGO_THRESHOLD_MS} (14 days) old,
     * else as {@code YYYY-MM-DD}.
     */
    public static String timestamp(long epochMillis) {
        long age = System.currentTimeMillis() - epochMillis;
        if (age >= 0 && age < AGO_THRESHOLD_MS) {
            return ago(age);
        }
        return DATE.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()));
    }

    /** {@code epochMillis} as a plain {@code YYYY-MM-DD} date, for editable-field prefill. */
    public static String isoDate(long epochMillis) {
        return DATE.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()));
    }

    private static String ago(long ageMs) {
        long seconds = ageMs / 1000;
        if (seconds < 60) {
            return "just now";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }
        long days = hours / 24;
        return days + (days == 1 ? " day ago" : " days ago");
    }
}
