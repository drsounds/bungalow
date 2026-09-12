package se.spacify.net.cache;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A resource resolved through {@link RequestStore} — either served straight
 * from the persistent cache ({@link #fromCache()}) or just fetched.
 */
public final class FetchedResource {

    private final String uri;
    private final String contentType;
    private final byte[] body;
    private final long fetchedAt;
    private final boolean fromCache;

    FetchedResource(String uri, String contentType, byte[] body, long fetchedAt, boolean fromCache) {
        this.uri = uri;
        this.contentType = contentType;
        this.body = body;
        this.fetchedAt = fetchedAt;
        this.fromCache = fromCache;
    }

    public String uri()         { return uri; }
    public String contentType() { return contentType; }
    public byte[] body()        { return body; }

    /** When this resource was last actually fetched (not when this call returned it). */
    public long fetchedAt() { return fetchedAt; }

    /** True if this call was answered from the persistent cache without a new fetch/provider call. */
    public boolean fromCache() { return fromCache; }

    public String asText() { return asText(StandardCharsets.UTF_8); }

    public String asText(Charset charset) {
        return body == null ? null : new String(body, charset);
    }
}
