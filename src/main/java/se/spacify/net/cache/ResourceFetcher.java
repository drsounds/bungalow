package se.spacify.net.cache;

import java.io.IOException;

/**
 * Per-scheme resolver plugged into {@link RequestStore}. A fetcher is the only
 * place that knows how to actually go get a resource's bytes — over HTTP, from
 * an internal provider, or any future scheme a plugin adds via
 * {@link RequestStore#registerFetcher} — while {@code RequestStore} itself stays
 * protocol-agnostic and only owns caching/persistence.
 */
public interface ResourceFetcher {

    /** Whether this fetcher handles {@code scheme} (e.g. {@code "https"}, {@code "spacify"}). */
    boolean supports(String scheme);

    /**
     * Fetch {@code uri} fresh. {@code cached} is the previous cache entry for this
     * URI, or {@code null} if there is none — a fetcher that supports conditional
     * requests (HTTP ETag/If-Modified-Since) uses it to return
     * {@link Outcome#unchanged()} instead of re-downloading the body.
     *
     * @throws IOException on any transient failure (network, timeout, non-2xx, no
     *         matching internal provider); {@link RequestStore} falls back to
     *         serving a stale cache entry when one exists.
     */
    Outcome fetch(String uri, CachedResource cached) throws IOException;

    /** The outcome of one fetch attempt. */
    record Outcome(boolean notModified, byte[] body, String contentType,
                    String etag, String lastModified, Long ttlMillis, boolean noStore) {

        /** The resource is unchanged since {@code cached} — reuse it, just refresh its freshness window. */
        public static Outcome unchanged() {
            return new Outcome(true, null, null, null, null, null, false);
        }

        /** A fresh body with no cache-control metadata; {@link RequestStore}'s default TTL applies. */
        public static Outcome of(byte[] body, String contentType) {
            return new Outcome(false, body, contentType, null, null, null, false);
        }

        /** A fresh body with revalidation metadata and/or an explicit TTL (either may be {@code null}). */
        public static Outcome of(byte[] body, String contentType, String etag,
                                  String lastModified, Long ttlMillis) {
            return new Outcome(false, body, contentType, etag, lastModified, ttlMillis, false);
        }

        /** A fresh body the caller explicitly asked not to be persisted (e.g. HTTP {@code Cache-Control: no-store}). */
        public static Outcome noStore(byte[] body, String contentType) {
            return new Outcome(false, body, contentType, null, null, null, true);
        }
    }
}
