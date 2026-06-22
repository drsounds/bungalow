package se.spacify.plugin.search;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.plugin.Plugin;
import se.spacify.plugin.PluginContext;
import se.spacify.plugin.search.views.SearchView;

/**
 * Built-in plugin owning the unified search screen ({@code spacify:search}). The
 * view aggregates every registered {@code SearchProvider}; other plugins
 * contribute their entities by registering providers (the "Searchable" model).
 */
public class SearchPlugin extends Plugin {

    @Override public String getId()   { return "search"; }
    @Override public String getName() { return "Search"; }
    @Override public void onRegister(AspectManager<? extends Aspect> aspectManager) {}

    @Override
    public void onActivate(PluginContext ctx) {
        ctx.registerView(new SearchView(ctx.viewStack()));
    }
}
