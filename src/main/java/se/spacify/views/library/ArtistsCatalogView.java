package se.spacify.views.library;

import se.spacify.db.entity.Artist;
import se.spacify.navigation.SPViewStack;
import se.spacify.service.catalogue.MusicCatalogueService;
import se.spacify.ui.MainWindow;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Catalogue artists view — the searchable entry point into a remote catalogue.
 * Typing a query searches the catalogue's artists; double-clicking an artist
 * drills into that catalogue's Releases scoped to the artist.
 */
public class ArtistsCatalogView extends AbstractCatalogView<Artist> {

    public ArtistsCatalogView(SPViewStack viewStack) { super(viewStack); }

    @Override protected String kind() { return "artists"; }
    @Override protected String searchHint() { return "Search artists…"; }
    @Override protected String[] getColumns() { return new String[]{"Artist"}; }

    @Override
    protected List<Artist> fetch(MusicCatalogueService svc) {
        return query().isBlank() ? List.of() : svc.searchArtists(query());
    }

    @Override
    protected Object[] toRow(Artist a) {
        return new Object[]{ a.getName() };
    }

    @Override
    protected void onActivate(int row) {
        Artist a = rows.get(row);
        if (a.getMbid() != null) {
            open("releases?artist=" + URLEncoder.encode(a.getMbid(), StandardCharsets.UTF_8));
        }
    }

    @Override
    protected String emptyHeader(MusicCatalogueService svc) {
        return query().isBlank()
            ? "Search the " + svc.getName() + " catalogue"
            : svc.getName() + " — no artists for \"" + query() + "\"";
    }

    @Override public String getTitle() { return "Catalogue Artists"; }
}
