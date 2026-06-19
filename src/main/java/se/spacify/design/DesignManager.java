package se.spacify.design;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import se.spacify.aspect.AspectManager;
import se.spacify.service.AuthAspect;
import se.spacify.ui.theme.Theme;

public class DesignManager implements AspectManager<Design> {
    
    private static DesignManager instance;

    private final Map<String, Design> designs = new LinkedHashMap<>();

    public static DesignManager getInstance() {
        if (instance == null) instance = new DesignManager();
        return instance;
    }

    // ── Design registration ──────────────────────────────────────────────────

    public void register(Design design) {
        designs.put(design.getId(), design);
    }

    public void unregister(String designId) {
        Design s = designs.remove(designId);
    }

    /** Unregister a specific design instance (e.g. when a plugin is disabled). */
    public void unregister(Design design) {
        if (design != null) unregister(design.getId());
    }
 
    /**
     * Returns the first registered design exposing the given aspect, or null.
     * {@code aspect} may be {@link Design} itself, a concrete design class, or
     * any capability interface (e.g. {@link se.spacify.design.media.MediaDesign},
     * {@link AuthAspect}); only the designs implementing it are considered.
     */
    @SuppressWarnings("unchecked")
    public <T> T getDesign(Class<T> aspect) {
        for (Design s : designs.values())
            if (aspect.isInstance(s)) return (T) s;
        return null;
    }

    /** Returns all registered designs exposing the given aspect. */
    @SuppressWarnings("unchecked")
    public <T> List<T> getDesigns(Class<T> aspect) {
        List<T> result = new ArrayList<>();
        for (Design s : designs.values())
            if (aspect.isInstance(s)) result.add((T) s);
        return result;
    }

    public Collection<Design> all() { return Collections.unmodifiableCollection(designs.values()); }


    public Design get(String id) {
        return designs.get(id);
    }

}
