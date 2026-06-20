package se.spacify.views.library;

import se.spacify.db.DatabaseManager;
import se.spacify.db.entity.Recording;
import se.spacify.library.LibraryEvents;
import se.spacify.navigation.ViewStack;
import se.spacify.plugin.catalogue.service.MusicCatalogueService;
import se.spacify.service.media.PlayRequest;

import java.util.List;

/**
 * Catalogue recordings (tracks) view. Populated by drilling in from a release
 * ({@code ?release=<mbid>}) or by typing a search. Double-clicking a row plays it
 * through the "Play with…" resolver; the leading ＋/✓ column toggles whether the
 * track is saved in the local library, Spotify-style.
 */
public class RecordingsCatalogView extends AbstractCatalogView<Recording> {

    private static final String ADD = "＋";
    private static final String IN  = "✓";

    public RecordingsCatalogView(ViewStack viewStack) {
        super(viewStack);
        // Keep the toggle column tight — it holds just the ＋/✓ glyph.
        table.getColumnModel().getColumn(0).setMaxWidth(34);
        table.getColumnModel().getColumn(0).setMinWidth(34);
    }

    @Override protected String kind() { return "recordings"; }
    @Override protected String searchHint() { return "Search recordings…"; }
    @Override protected String[] getColumns() { return new String[]{"", "Recording", "Length"}; }

    @Override
    protected List<Recording> fetch(MusicCatalogueService svc) {
        String release = param("release");
        if (release != null) return svc.browseRecordingsByRelease(release, 0, 100);
        return query().isBlank() ? List.of() : svc.searchRecordings(query());
    }

    @Override
    protected Object[] toRow(Recording r) {
        return new Object[]{ inLibrary(r) ? IN : ADD, r.getTitle(), fmtDuration(r.getDurationMs()) };
    }

    @Override
    protected PlayRequest playRequestAt(int row) {
        Recording r = rows.get(row);
        // Transient catalogue recording — no local Track; keyed by ISRC/URI.
        return new PlayRequest(null, r.getIsrc(), r.getTitle(), "", r.getPlayUri(), r.getDurationMs());
    }

    @Override
    protected void onCellClicked(int row, int col) {
        if (col != 0 || row < 0 || row >= rows.size()) return;
        Recording r = rows.get(row);
        if (inLibrary(r)) removeFromLibrary(r); else addToLibrary(r);
        model.setValueAt(inLibrary(r) ? IN : ADD, row, 0);
        LibraryEvents.fireChanged();
    }

    @Override
    protected String emptyHeader(MusicCatalogueService svc) {
        if (param("release") != null) return svc.getName() + " — no recordings on this release";
        return query().isBlank()
            ? "Pick a release, or search " + svc.getName() + " recordings"
            : svc.getName() + " — no recordings for \"" + query() + "\"";
    }

    // ── Local-library membership (by ISRC when present, else title) ─────────────

    private boolean inLibrary(Recording r) {
        return findLocal(r) != null;
    }

    private Recording findLocal(Recording r) {
        try {
            if (r.getIsrc() != null) {
                List<Recording> byIsrc = DatabaseManager.getInstance().recordingDao()
                        .queryForEq("isrc", r.getIsrc());
                if (!byIsrc.isEmpty()) return byIsrc.get(0);
            }
            List<Recording> byTitle = DatabaseManager.getInstance().recordingDao()
                    .queryForEq("title", r.getTitle());
            return byTitle.isEmpty() ? null : byTitle.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    private void addToLibrary(Recording r) {
        try {
            Recording copy = new Recording(r.getTitle());
            copy.setIsrc(r.getIsrc());
            copy.setDurationMs(r.getDurationMs());
            DatabaseManager.getInstance().recordingDao().create(copy);
        } catch (Exception e) {
            showError(e);
        }
    }

    private void removeFromLibrary(Recording r) {
        try {
            Recording local = findLocal(r);
            if (local != null) DatabaseManager.getInstance().recordingDao().delete(local);
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override public String getTitle() { return "Catalogue Recordings"; }
}
