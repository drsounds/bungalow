package se.spacify.net.cache;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;

/**
 * Fetches {@code http:}/{@code https:} resources with real conditional-GET
 * support: a prior cache entry's ETag/Last-Modified are sent back as
 * If-None-Match/If-Modified-Since, so a still-fresh-upstream resource costs one
 * 304 round trip and no body transfer. TTL comes from the response's
 * {@code Cache-Control: max-age} when present ({@code no-store} disables
 * persistence entirely); otherwise {@link RequestStore}'s default TTL applies.
 */
public final class HttpResourceFetcher implements ResourceFetcher {

    private final HttpClient http;

    public HttpResourceFetcher() {
        this(HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    public HttpResourceFetcher(HttpClient http) {
        this.http = http;
    }

    @Override
    public boolean supports(String scheme) {
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    @Override
    public Outcome fetch(String uri, CachedResource cached) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(uri))
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "Spacify");
        if (cached != null) {
            if (cached.getEtag() != null)         builder.header("If-None-Match", cached.getEtag());
            if (cached.getLastModified() != null) builder.header("If-Modified-Since", cached.getLastModified());
        }

        HttpResponse<byte[]> resp;
        try {
            resp = http.send(builder.GET().build(), HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted fetching " + uri, e);
        }

        int status = resp.statusCode();
        if (status == 304 && cached != null) {
            return Outcome.unchanged();
        }
        if (status < 200 || status >= 300) {
            throw new IOException("HTTP " + status + " fetching " + uri);
        }

        String contentType = resp.headers().firstValue("Content-Type").orElse(null);
        String cacheControl = resp.headers().firstValue("Cache-Control").orElse("");
        if (cacheControl.toLowerCase(Locale.ROOT).contains("no-store")) {
            return Outcome.noStore(resp.body(), contentType);
        }

        String etag         = resp.headers().firstValue("ETag").orElse(null);
        String lastModified = resp.headers().firstValue("Last-Modified").orElse(null);
        Long ttlMillis       = maxAgeMillis(cacheControl);
        return Outcome.of(resp.body(), contentType, etag, lastModified, ttlMillis);
    }

    /** {@code Cache-Control: max-age=N} in milliseconds, or {@code null} if absent/unparsable. */
    private static Long maxAgeMillis(String cacheControl) {
        for (String directive : cacheControl.split(",")) {
            directive = directive.trim();
            if (directive.toLowerCase(Locale.ROOT).startsWith("max-age=")) {
                try {
                    return Long.parseLong(directive.substring(8).trim()) * 1000L;
                } catch (NumberFormatException ignored) {
                    // fall through to null
                }
            }
        }
        return null;
    }
}
