package se.spacify.service;

import se.spacify.navigation.SPViewStack;
import se.spacify.navigation.SidebarNode;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

/**
 * Central singleton registry for Services and Features.
 * Call register() for each, then startAll() to drive onCreate → onStart.
 */
public class ServiceManager {

    private static ServiceManager instance;

    private final Map<String, Service> services = new LinkedHashMap<>();
    private final List<Feature>        features  = new ArrayList<>();

    private ServiceManager() {}

    public static ServiceManager getInstance() {
        if (instance == null) instance = new ServiceManager();
        return instance;
    }

    // ── Service registration ──────────────────────────────────────────────────

    public void register(Service service) {
        services.put(service.getId(), service);
        service.onCreate();
    }

    public void unregister(String serviceId) {
        Service s = services.remove(serviceId);
        if (s != null) { s.onStop(); s.onDestroy(); }
    }

    /** Unregister a specific service instance (e.g. when a plugin is disabled). */
    public void unregister(Service service) {
        if (service != null) unregister(service.getId());
    }

    public void startAll() {
        for (Service s : services.values()) s.onStart();
    }

    public void stopAll() {
        for (Service s : services.values()) s.onStop();
    }

    public void shutdownAll() {
        for (Service s : services.values()) { s.onStop(); s.onDestroy(); }
        services.clear();
    }

    /**
     * Returns the first registered service exposing the given aspect, or null.
     * {@code aspect} may be {@link Service} itself, a concrete service class, or
     * any capability interface (e.g. {@link se.spacify.service.media.MediaService},
     * {@link AuthAspect}); only the services implementing it are considered.
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> aspect) {
        for (Service s : services.values())
            if (aspect.isInstance(s)) return (T) s;
        return null;
    }

    /** Returns all registered services exposing the given aspect. */
    @SuppressWarnings("unchecked")
    public <T> List<T> getServices(Class<T> aspect) {
        List<T> result = new ArrayList<>();
        for (Service s : services.values())
            if (aspect.isInstance(s)) result.add((T) s);
        return result;
    }

    public Collection<Service> allServices() { return Collections.unmodifiableCollection(services.values()); }

    // ── Feature registration ──────────────────────────────────────────────────

    public void register(Feature feature) {
        features.add(feature);
        feature.onRegister(this);
    }

    /** Drop a feature from the registry (its views/nodes are removed by the caller). */
    public void unregisterFeature(Feature feature) {
        features.remove(feature);
    }

    /**
     * Wire all registered features into the running UI.
     * Call this after MainWindow has been built and the SPViewStack is live.
     *
     * @param viewStack     the app's main view stack
     * @param sidebarRoot   root node of the sidebar JTree model
     */
    public void activateFeatures(SPViewStack viewStack, DefaultMutableTreeNode sidebarRoot) {
        for (Feature f : features) {
            f.getViews().forEach(viewStack::registerView);
            for (SidebarNode sn : f.getSidebarNodes()) {
                sidebarRoot.add(buildTreeNode(sn));
            }
        }
    }

    private static DefaultMutableTreeNode buildTreeNode(SidebarNode sn) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(sn);
        for (SidebarNode child : sn.getChildren()) node.add(buildTreeNode(child));
        return node;
    }

    public List<Feature> allFeatures() { return Collections.unmodifiableList(features); }
}
