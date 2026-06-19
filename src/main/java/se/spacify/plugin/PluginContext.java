package se.spacify.plugin;

import se.spacify.design.Design;
import se.spacify.navigation.SPView;
import se.spacify.navigation.SPViewStack;
import se.spacify.navigation.SidebarNode;
import se.spacify.service.Service;
import se.spacify.service.Feature;
import se.spacify.skinning.Skin;
import se.spacify.ui.chrome.Chrome;
import se.spacify.ui.theme.Theme;

/**
 * Registration surface handed to {@link Plugin#onActivate}. Every contribution
 * made through this context is recorded, so disabling or removing the plugin
 * can undo exactly what it added (Services, features, views, sidebar nodes).
 */
public interface PluginContext {

    /** This plugin's java-namespace id, e.g. {@code se.spacify.plugin.library}. */
    String pluginId();
    
    void registerChrome(Chrome c);

    void registerDesign(Design d);
    void registerTheme(Theme t);

    void registerService(Service s);

    /** Register a Service (its {@code onCreate}/{@code onStart} are invoked). */
    void registerSkin(Skin s);

    /** Register a feature; its views and sidebar nodes are wired automatically. */
    void registerFeature(Feature f);

    /** Register a view into the {@code spacify:} URI space. */
    void registerView(SPView v);

    /** The app view stack, for views (e.g. the web view) that drive navigation. */
    SPViewStack viewStack();

    /** Append a node (subtree) to the sidebar; the handle maintains dynamic children. */
    SidebarHandle addSidebarNode(SidebarNode n);

    /** Typed, persisted settings for this plugin (per its declared schema). */
    PluginSettings settings();
}
