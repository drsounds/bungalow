package se.spacify.ui.chrome;

import java.util.*;

/**
 * Central singleton registry for Services and Features.
 * Call register() for each, then startAll() to drive onCreate → onStart.
 */
public class ChromeManager {

    private static ChromeManager instance;

    private final Map<String, Chrome> chromes = new LinkedHashMap<>();

    private ChromeManager() {}

    public static ChromeManager getInstance() {
        if (instance == null) instance = new ChromeManager();
        return instance;
    }

    // ── Service registration ──────────────────────────────────────────────────

    public void register(Chrome chrome) {
        chromes.put(chrome.getId(), chrome);
    }

    public void unregister(String chromeId) {
        Chrome c = chromes.remove(chromeId);
        if (c != null) { c.onDestroy(); }
    }

    /** Unregister a specific service instance (e.g. when a plugin is disabled). */
    public void unregister(Chrome chrome) {
        if (chrome != null) unregister(chrome.getId());
    } 

    /**
     * Returns the first registered service exposing the given aspect, or null.
     * {@code aspect} may be {@link Service} itself, a concrete service class, or
     * any capability interface (e.g. {@link se.spacify.service.media.MediaService},
     * {@link AuthAspect}); only the services implementing it are considered.
     */
    @SuppressWarnings("unchecked")
    public <T> T getChrome(Class<T> aspect) {
        for (Chrome s : chromes.values())
            if (aspect.isInstance(s)) return (T) s;
        return null;
    }

    /** Returns all registered services exposing the given aspect. */
    @SuppressWarnings("unchecked")
    public <T> List<T> getChromes(Class<T> aspect) {
        List<T> result = new ArrayList<>();
        for (Chrome s : chromes.values())
            if (aspect.isInstance(s)) result.add((T) s);
        return result;
    }

    public Collection<Chrome> allChromes() { return Collections.unmodifiableCollection(chromes.values()); }

}
