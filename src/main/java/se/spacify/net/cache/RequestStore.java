package se.spacify.net.cache;

import se.spacify.db.DatabaseManager;
import se.spacify.net.Uri;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The app-wide, protocol-agnostic store for every request the app makes for a
 * named resource: {@code https://…} covers artwork, MusicBrainz/YouTube API
 * calls and favicons; {@code spacify:…} covers data an app/plugin owns
 * internally. One call — {@link #get(String)} — resolves either kind: a fresh
 * fetch is dispatched to whichever registered {@link ResourceFetcher} handles
 * the URI's scheme, and the result is written through to the
 * {@code cached_resources} SQLite table (via {@link DatabaseManager}), so it
 * survives an app restart — a cold-started app reuses yesterday's
 * favicons/album art/API responses instead of re-fetching everything.
 *
 * <p>Freshness: HTTP resources honour {@code Cache-Control: max-age} (and
 * ETag/Last-Modified for a cheap 304 revalidation) when the server sends them,
 * else fall back to {@link #DEFAULT_TTL}; internal resources use their
 * provider's declared TTL. A fetch that fails with a stale cache entry on hand
 * serves the stale entry rather than failing outright, which keeps the app
 * usable offline.
 *
 * <p>Add support for a new scheme with {@link #registerFetcher}; add a data
 * source under the existing {@code spacify:} scheme with
 * {@link #registerInternalResourceProvider}.
 *
 * <p>Never used to cache mutating requests (saves/deletes/postbacks): those
 * aren't resources, and no {@link ResourceFetcher} in this store should ever
 * be written to treat them as one.
 */
public final class RequestStore {

    /** Applied when a fetch didn't specify its own TTL (e.g. an HTTP response with no Cache-Control). */
    public static final Duration DEFAULT_TTL = Duration.ofHours(1);

    private static final RequestStore INSTANCE = new RequestStore();

    public static RequestStore getInstance() { return INSTANCE; }

    private final List<ResourceFetcher> fetchers = new CopyOnWriteArrayList<>();
    private final SpacifyResourceFetcher spacifyFetcher = new SpacifyResourceFetcher();

    private RequestStore() {
        fetchers.add(new HttpResourceFetcher());
        fetchers.add(spacifyFetcher);
    }

    /**
     * Add support for another URI scheme. Checked before the built-ins, so a
     * plugin may also override how {@code http(s)}/{@code spacify} are handled.
     */
    public void registerFetcher(ResourceFetcher fetcher) {
        fetchers.add(0, fetcher);
    }

    /** Register a data source for {@code spacify:} resource requests (see {@link InternalResourceProvider}). */
    public void registerInternalResourceProvider(InternalResourceProvider provider) {
        spacifyFetcher.register(provider);
    }

    /** Undo {@link #registerInternalResourceProvider} (e.g. when a plugin is disabled). */
    public void unregisterInternalResourceProvider(InternalResourceProvider provider) {
        spacifyFetcher.unregister(provider);
    }

    // ── Lookup ───────────────────────────────────────────────────────────────────

    /** {@code get(uri, false)}. */
    public FetchedResource get(String uri) {
        return get(uri, false);
    }

    /**
     * Resolve {@code uri}: serve the persistent cache when it's still fresh,
     * otherwise dispatch to the {@link ResourceFetcher} registered for its
     * scheme and cache the result.
     *
     * @param forceRefresh skip the freshness check and always revalidate/refetch
     * @throws RequestStoreException no fetcher is registered for the URI's
     *         scheme, or the fetch failed with nothing usable cached to fall
     *         back to
     */
    public FetchedResource get(String uri, boolean forceRefresh) {
        String scheme = schemeOf(uri);
        ResourceFetcher fetcher = fetcherFor(scheme);
        if (fetcher == null) {
            throw new RequestStoreException("No ResourceFetcher registered for scheme '" + scheme + "' (" + uri + ")");
        }

        CachedResource cached = lookup(uri);
        long now = System.currentTimeMillis();
        if (!forceRefresh && cached != null && cached.getBody() != null && now < cached.getExpiresAt()) {
            return toResource(cached, true);
        }

        try {
            ResourceFetcher.Outcome outcome = fetcher.fetch(uri, forceRefresh ? null : cached);
            if (outcome.notModified() && cached != null) {
                cached.setFetchedAt(now);
                cached.setExpiresAt(now + DEFAULT_TTL.toMillis());
                save(cached);
                return toResource(cached, true);
            }
            long ttl = outcome.ttlMillis() != null ? outcome.ttlMillis() : DEFAULT_TTL.toMillis();
            CachedResource entry = new CachedResource(uri, scheme, outcome.contentType(),
                    outcome.etag(), outcome.lastModified(), outcome.body(), now, now + ttl);
            if (!outcome.noStore()) {
                save(entry);
            }
            return toResource(entry, false);
        } catch (IOException e) {
            if (cached != null && cached.getBody() != null) {
                System.err.println("RequestStore: fetch failed for " + uri + " (" + e.getMessage()
                        + "), serving stale cache");
                return toResource(cached, true);
            }
            throw new RequestStoreException("Fetch failed for " + uri, e);
        }
    }

    /** {@link #get(String)} off the calling thread — fetches are blocking network/provider calls. */
    public CompletableFuture<FetchedResource> getAsync(String uri) {
        return CompletableFuture.supplyAsync(() -> get(uri));
    }

    // ── Cache management ─────────────────────────────────────────────────────────

    /** Drop one cached entry, forcing the next {@link #get} to fetch it fresh. */
    public void evict(String uri) {
        try {
            DatabaseManager.getInstance().dao(CachedResource.class, String.class).deleteById(uri);
        } catch (Exception ignored) {
            // Best-effort: an eviction that fails just leaves a stale entry in place.
        }
    }

    /** Drop every cached entry whose TTL has passed. Housekeeping only — nothing calls this automatically. */
    public void clearExpired() {
        try {
            var dao = DatabaseManager.getInstance().dao(CachedResource.class, String.class);
            dao.delete(dao.queryBuilder().where().lt("expires_at", System.currentTimeMillis()).query());
        } catch (Exception ignored) {
            // Best-effort housekeeping.
        }
    }

    /** Drop the entire cache. */
    public void clearAll() {
        try {
            DatabaseManager.getInstance().dao(CachedResource.class, String.class).deleteBuilder().delete();
        } catch (Exception ignored) {
            // Best-effort.
        }
    }

    // ── Internals ────────────────────────────────────────────────────────────────

    private ResourceFetcher fetcherFor(String scheme) {
        for (ResourceFetcher f : fetchers) {
            if (f.supports(scheme)) return f;
        }
        return null;
    }

    private static String schemeOf(String uri) {
        try {
            String scheme = Uri.parse(uri).getScheme();
            if (scheme == null) {
                throw new IllegalArgumentException("Request URI has no scheme: " + uri);
            }
            return scheme;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Malformed request URI: " + uri, e);
        }
    }

    private CachedResource lookup(String uri) {
        try {
            return DatabaseManager.getInstance().dao(CachedResource.class, String.class).queryForId(uri);
        } catch (Exception e) {
            return null;   // DB not initialised yet, or a lookup hiccup — treat as a cache miss
        }
    }

    private void save(CachedResource entry) {
        try {
            DatabaseManager.getInstance().dao(CachedResource.class, String.class).createOrUpdate(entry);
        } catch (Exception e) {
            System.err.println("RequestStore: cache persist failed for " + entry.getUri() + ": " + e);
        }
    }

    private static FetchedResource toResource(CachedResource c, boolean fromCache) {
        return new FetchedResource(c.getUri(), c.getContentType(), c.getBody(), c.getFetchedAt(), fromCache);
    }
}
