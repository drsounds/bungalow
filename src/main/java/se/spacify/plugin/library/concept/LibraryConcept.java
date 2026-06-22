package se.spacify.plugin.library.concept;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.concept.Concept;
import se.spacify.concept.ConceptContext;
import se.spacify.db.DatabaseManager;
import se.spacify.db.entity.Artist;
import se.spacify.db.entity.Release;
import se.spacify.library.LibraryEvents;
import se.spacify.navigation.ViewStack;
import se.spacify.navigation.SidebarNode;
import se.spacify.plugin.Plugin;
import se.spacify.plugin.SidebarHandle;
import se.spacify.plugin.library.views.ArtistDetailView;
import se.spacify.plugin.library.views.ArtistsLibraryView;
import se.spacify.plugin.library.views.LocalFileLibraryView;
import se.spacify.plugin.library.views.RecordingsLibraryView;
import se.spacify.plugin.library.views.ReleaseDetailView;
import se.spacify.plugin.library.views.ReleasesLibraryView;
import se.spacify.plugin.library.views.TracksLibraryView;
import se.spacify.plugin.playlist.views.PlaylistView;
import se.spacify.plugin.search.views.SearchView;

public class LibraryConcept implements Concept {
    private Plugin plugin;
    public Plugin getPlugin() {
        return plugin;
    }

    public ViewStack getViewStack() {
        return plugin.getViewStack();
    }

    public LibraryConcept(Plugin plugin) {
        this.plugin = plugin;
    }

    private final Runnable refresh = this::refresh;
    private SidebarHandle releases;
    private SidebarHandle artists;
    @Override
    public void onActivate(ConceptContext ctx) {
        // The search view is owned by the search plugin now; the library only
        // contributes its entries as a search provider.
        ctx.registerSearchProvider(new se.spacify.plugin.library.LibrarySearchProvider());
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
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // TODO Auto-generated method stub
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
    public Icon getIcon() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getIcon'");
    }

}
