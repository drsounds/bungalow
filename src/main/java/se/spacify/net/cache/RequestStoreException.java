package se.spacify.net.cache;

/**
 * Thrown by {@link RequestStore#get} when a request URI can't be resolved at
 * all — no {@link ResourceFetcher} registered for its scheme, or the fetch
 * failed and there was no stale cache entry to fall back on.
 */
public class RequestStoreException extends RuntimeException {

    public RequestStoreException(String message) {
        super(message);
    }

    public RequestStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
