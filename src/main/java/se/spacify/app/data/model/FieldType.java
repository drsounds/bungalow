package se.spacify.app.data.model;

import java.util.Locale;

/**
 * The scalar kinds a {@link DataField} can hold. Display formatting and the
 * {@code _uri}/{@code _uris} naming convention (see {@link DataField}) are keyed off
 * this.
 */
public enum FieldType {
    /** Free text, shown as-is. */
    TEXT,
    /**
     * One or more URIs (its {@link DataField#getSlug()} must end {@code _uri} for a
     * single value or {@code _uris} for many); rendered as clickable links, and is
     * how a related table points back at the row it belongs to.
     */
    LINK,
    /** Whole number, pretty-printed with grouping separators in list views. */
    NUMBER,
    /** Decimal number, pretty-printed with grouping and two decimals in list views. */
    FLOAT,
    /** Epoch-millis instant, printed as "N days ago" (&lt; 14 days) or {@code YYYY-MM-DD}. */
    TIMESTAMP;

    /** Parse a user-typed type name (case-insensitive), or fail with a friendly message. */
    public static FieldType parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("field type is required (text, link, number, float, timestamp)");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        for (FieldType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException(
            "unknown field type '" + raw + "' (expected text, link, number, float or timestamp)");
    }
}
