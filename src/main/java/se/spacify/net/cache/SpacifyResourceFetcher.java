package se.spacify.net.cache;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Resolves {@code spacify:} resource requests by delegating to whichever
 * registered {@link InternalResourceProvider} claims the URI. There is no
 * conditional-GET concept here (there's no network round trip to save) — a
 * provider simply gets re-invoked once its own
 * {@link InternalResourceProvider#ttl() ttl} has elapsed.
 */
public final class SpacifyResourceFetcher implements ResourceFetcher {

    private final List<InternalResourceProvider> providers = new CopyOnWriteArrayList<>();

    public void register(InternalResourceProvider provider) {
        providers.add(provider);
    }

    public void unregister(InternalResourceProvider provider) {
        providers.remove(provider);
    }

    @Override
    public boolean supports(String scheme) {
        return "spacify".equalsIgnoreCase(scheme);
    }

    @Override
    public Outcome fetch(String uri, CachedResource cached) throws IOException {
        for (InternalResourceProvider provider : providers) {
            if (provider.accepts(uri)) {
                byte[] body = provider.fetch(uri);
                if (body == null) {
                    throw new IOException("Internal resource provider found no resource at " + uri);
                }
                return Outcome.of(body, provider.contentType(uri), null, null, provider.ttl().toMillis());
            }
        }
        throw new IOException("No InternalResourceProvider registered for " + uri);
    }
}
