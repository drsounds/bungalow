package se.spacify.plugin.search;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;

import se.spacify.library.LibraryEvents;

import se.spacify.plugin.Plugin;
import se.spacify.plugin.PluginContext;

import se.spacify.plugin.search.concept.SearchConcept;

/**
 * Built-in plugin providing the music library: the data-backed views (tracks,
 * recordings, releases, artists, local files, detail pages, search, playlists)
 * registered into the {@code spacify:library*} URI space, plus the "Your Library"
 * sidebar subtree with live Releases/Artists lists kept in sync via
 * {@link LibraryEvents}.
 */
public class SearchPlugin extends Plugin {


    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "music";
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Music";
    }

    @Override
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // TODO Auto-generated method stub

    }
    @Override
    public void onActivate(PluginContext ctx) {
        ctx.registerConcept(new SearchConcept(this));
    }
}
