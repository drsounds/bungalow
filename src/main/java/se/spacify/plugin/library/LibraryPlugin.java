package se.spacify.plugin.library;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.db.DatabaseManager;
import se.spacify.db.entity.Artist;
import se.spacify.db.entity.Release;
import se.spacify.library.LibraryEvents;
import se.spacify.navigation.SidebarNode;
import se.spacify.plugin.Plugin;
import se.spacify.plugin.PluginContext;
import se.spacify.plugin.SidebarHandle;
import se.spacify.views.PlaylistView;
import se.spacify.views.SearchView;
import se.spacify.views.library.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Built-in plugin providing the music library: the data-backed views (tracks,
 * recordings, releases, artists, local files, detail pages, search, playlists)
 * registered into the {@code spacify:library*} URI space, plus the "Your Library"
 * sidebar subtree with live Releases/Artists lists kept in sync via
 * {@link LibraryEvents}.
 */
public class LibraryPlugin extends Plugin {

    private final Runnable refresh = this::refresh;
    private SidebarHandle releases;
    private SidebarHandle artists;

    @Override
    public void onActivate(PluginContext ctx) {
        ctx.registerView(new SearchView(getViewStack()));
        ctx.registerView(new TracksLibraryView(getViewStack()));
        ctx.registerView(new RecordingsLibraryView(getViewStack()));
        ctx.registerView(new ReleasesLibraryView(getViewStack()));
        ctx.registerView(new ArtistsLibraryView(getViewStack()));
        ctx.registerView(new LocalFileLibraryView(getViewStack()));
        ctx.registerView(new ReleaseDetailView(getViewStack()));
        ctx.registerView(new ArtistDetailView(getViewStack()));
        ctx.registerView(new PlaylistView(getViewStack()));

        SidebarNode lib = new SidebarNode("Your Library", "spacify:library");
        lib.addChild(new SidebarNode("Tracks",      "spacify:library:tracks"));
        lib.addChild(new SidebarNode("Recordings",  "spacify:library:recordings"));
        lib.addChild(new SidebarNode("Releases",    "spacify:library:releases"));
        lib.addChild(new SidebarNode("Artists",     "spacify:library:artists"));
        lib.addChild(new SidebarNode("Local Files", "spacify:library:local"));

        SidebarHandle libHandle = ctx.addSidebarNode(lib);
        releases = libHandle.child("spacify:library:releases");
        artists  = libHandle.child("spacify:library:artists");
        refresh();
        libHandle.expand();

        LibraryEvents.addListener(refresh);
    }

    @Override
    public void onDeactivate() {
        LibraryEvents.removeListener(refresh);
    }

    private void refresh() {
        if (releases != null) releases.setChildren(releaseNodes());
        if (artists  != null) artists.setChildren(artistNodes());
    }

    private static List<SidebarNode> releaseNodes() {
        List<SidebarNode> out = new ArrayList<>();
        try {
            for (Release r : DatabaseManager.getInstance().releaseDao().queryForAll()) {
                out.add(new SidebarNode(r.getTitle(), "spacify:library:release:" + r.getId()));
            }
        } catch (Exception ignored) {}
        return out;
    }

    private static List<SidebarNode> artistNodes() {
        List<SidebarNode> out = new ArrayList<>();
        try {
            for (Artist a : DatabaseManager.getInstance().artistDao().queryForAll()) {
                out.add(new SidebarNode(a.getName(), "spacify:library:artist:" + a.getId()));
            }
        } catch (Exception ignored) {}
        return out;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "library";
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Library";
    }

    @Override
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // TODO Auto-generated method stub

    }
}
