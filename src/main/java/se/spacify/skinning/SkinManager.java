package se.spacify.skinning;

import java.util.*;

/**
 * Central singleton registry for Skins and Features.
 * Call register() for each, then startAll() to drive onCreate → onStart.
 */
public class SkinManager {

    private static SkinManager instance;

    private final Map<String, Skin> skins = new LinkedHashMap<>();

    private SkinManager() {}

    public static SkinManager getInstance() {
        if (instance == null) instance = new SkinManager();
        return instance;
    }

    // ── Skin registration ──────────────────────────────────────────────────

    public void register(Skin skin) {
        skins.put(skin.getId(), skin);
    }

    public void unregister(String skinId) {
        Skin s = skins.remove(skinId);
    }

    /** Unregister a specific Skin instance (e.g. when a plugin is disabled). */
    public void unregister(Skin skin) {
        if (skin != null) unregister(skin.getId());
    }

    /**
     * Returns the first registered Skin exposing the given aspect, or null.
     * {@code aspect} may be {@link Skin} itself, a concrete Skin class, or
     * any capability interface (e.g. {@link se.spacify.Skin.media.MediaSkin},
     * {@link AuthAspect}); only the Skins implementing it are considered.
     */
    @SuppressWarnings("unchecked")
    public <T> T getSkin(Class<T> aspect) {
        for (Skin s : skins.values())
            if (aspect.isInstance(s)) return (T) s;
        return null;
    }

    /** Returns all registered Skins exposing the given aspect. */
    @SuppressWarnings("unchecked")
    public <T> List<T> getSkins(Class<T> aspect) {
        List<T> result = new ArrayList<>();
        for (Skin s : skins.values())
            if (aspect.isInstance(s)) result.add((T) s);
        return result;
    }

    public Collection<Skin> allSkins() { return Collections.unmodifiableCollection(skins.values()); }
 
}
