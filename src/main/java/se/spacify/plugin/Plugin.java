package se.spacify.plugin;

import javax.swing.Icon;

import se.spacify.aspect.Aspect;
import se.spacify.navigation.SPViewStack;

import java.util.List;

/**
 * Entry point a plugin's main class implements. Identity (id, name, version) is
 * carried by the {@link PluginManifest} (jar {@code META-INF/MANIFEST.MF}
 * headers, or {@link BuiltinPluginRegistry} for main-bundle plugins); the class
 * here supplies the display icon, an optional typed settings schema, and the
 * registration logic.
 *
 * <p>A plugin may contribute any mix of {@link se.spacify.service.Service}s,
 * {@link se.spacify.feature.Feature}s, {@link se.spacify.navigation.SPView}s
 * (each accepting a {@code spacify:} URI) and sidebar nodes — all wired through
 * the {@link PluginContext} so they can be cleanly removed again on disable.
 */
public abstract class Plugin implements Aspect {
    PluginManager manager;
    public PluginManager getManager() {
        return manager;
    }
    public Plugin() {
        super();
    }
    public SPViewStack getViewStack() {
        return manager.getMainWindow().getViewStack();
    }
    /** Register Services, features, views and sidebar nodes via {@code ctx}. */
    public void onActivate(PluginContext ctx) {}

    /** Release any resources acquired in {@link #onActivate}. */
    public void onDeactivate() {}

    /** Icon shown in the plugin manager; null falls back to a default. */
    public Icon getIcon() { return null; }

    /** Typed settings this plugin exposes; the manager renders an editor per entry. */
    public List<PluginSetting> getSettingsSchema() { return List.of(); }
}
