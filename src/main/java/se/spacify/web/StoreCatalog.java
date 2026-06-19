package se.spacify.web;

import java.util.List;

/**
 * Hardcoded list of known music-Service "stores" shown in the navigation bar's
 * stores dropdown. Selecting one opens it in the {@code SPServiceWebView} via a
 * {@code spacify:store:<host>} URI. (Temporary — a dynamic Service registry
 * will replace this later.)
 */
public final class StoreCatalog {

    public record Store(String name, String host) {
        public String uri() { return SiteUri.STORE_PREFIX + host; }
    }

    public static final List<Store> STORES = List.of(
        new Store("Subvert",  "www.subvert.fm"),
        new Store("Jamendo",  "www.jamendo.com"),
        new Store("Bandcamp", "www.bandcamp.com"),
        new Store("Last.fm",  "www.last.fm")
    );

    private StoreCatalog() {}
}
