package se.spacify.plugin;

import javax.swing.Icon;
import java.util.List;

/**
 * Entry point a plugin's main class implements. Identity (id, name, version) is
 * carried by the {@link PluginManifest} (jar {@code META-INF/MANIFEST.MF}
 * headers, or {@link BuiltinPluginRegistry} for main-bundle plugins); the class
 * here supplies the display icon, an optional typed settings schema, and the
 * registration logic.
 *
 * <p>A plugin may contribute any mix of {@link se.spacify.service.Service}s,
 * {@link se.spacify.service.Feature}s, {@link se.spacify.navigation.SPView}s
 * (each accepting a {@code spacify:} URI) and sidebar nodes — all wired through
 * the {@link PluginContext} so they can be cleanly removed again on disable.
 */
public interface Plugin {

    /** Register Services, features, views and sidebar nodes via {@code ctx}. */
    void onActivate(PluginContext ctx);

    /** Release any resources acquired in {@link #onActivate}. */
    default void onDeactivate() {}

    /** Icon shown in the plugin manager; null falls back to a default. */
    default Icon getIcon() { return null; }

    /** Typed settings this plugin exposes; the manager renders an editor per entry. */
    default List<PluginSetting> getSettingsSchema() { return List.of(); }
}
