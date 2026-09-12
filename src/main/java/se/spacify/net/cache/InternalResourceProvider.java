package se.spacify.net.cache;

import java.io.IOException;
import java.time.Duration;

/**
 * Produces bytes for {@code spacify:} URIs an app/plugin owns, so that data can
 * be requested and cached through {@link RequestStore} exactly like an
 * {@code https:} resource. Register an instance via
 * {@link RequestStore#registerInternalResourceProvider}.
 *
 * <p>This is a separate, lightweight path from the {@code spacify:}
 * <em>navigation</em> URIs handled by {@code se.spacify.navigation.View}/
 * {@code se.spacify.app.spider.controller.Controller} — those mount UI and can
 * carry mutating actions, so they are never cached. An
 * {@code InternalResourceProvider} is for {@code spacify:} URIs that name an
 * actual idempotent, cacheable piece of data (e.g. a row snapshot, a generated
 * thumbnail) — never one that saves or deletes anything.
 */
public interface InternalResourceProvider {

    /** Whether this provider produces bytes for {@code uri} (already known to be a {@code spacify:} URI). */
    boolean accepts(String uri);

    /**
     * Produce the resource fresh.
     *
     * @return the resource's bytes, or {@code null} if {@code uri} doesn't currently resolve to anything
     */
    byte[] fetch(String uri) throws IOException;

    /** MIME type of what {@link #fetch} returns for {@code uri}. */
    default String contentType(String uri) { return "application/octet-stream"; }

    /** How long a fetched value stays fresh before this provider is asked again. */
    default Duration ttl() { return Duration.ofMinutes(5); }
}
