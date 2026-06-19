package se.spacify.views.library;

import se.spacify.db.DatabaseManager;
import se.spacify.db.LibraryRepository;
import se.spacify.db.entity.Artist;
import se.spacify.db.entity.Recording;
import se.spacify.db.entity.RecordingArtistCredit;
import se.spacify.navigation.SPViewStack;
import se.spacify.service.media.PlaybackCoordinator;
import se.spacify.ui.MainWindow;
import se.spacify.service.media.PlayQueueItem;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read-only detail page for a single artist, reached from the sidebar via
 * {@code spacify:library:artist:<id>}. Lists the artist's recordings and the
 * album each appears on; double-click resolves and plays across Services.
 */
public class ArtistDetailView extends AbstractLibraryView {

    public ArtistDetailView(SPViewStack viewStack) {
        super(viewStack);
    }
    private static final Pattern URI = Pattern.compile("spacify:library:artist:(\\d+)");

    private final List<Recording> rows = new ArrayList<>();
    private int artistId = -1;

    @Override protected boolean isEditable() { return false; }

    @Override protected String[] getColumns() {
        return new String[]{"Recording", "Album"};
    }

    @Override
    public void navigate(String uri) {
        Matcher m = URI.matcher(uri);
        artistId = m.matches() ? Integer.parseInt(m.group(1)) : -1;
    }

    @Override
    protected void reload() {
        rows.clear();
        model.setRowCount(0);
        if (artistId < 0) { setHeader(null); return; }
        try {
            Artist artist = DatabaseManager.getInstance().artistDao().queryForId(artistId);
            if (artist == null) { setHeader("Artist not found"); return; }
            setHeader(artist.getName());

            List<RecordingArtistCredit> credits = DatabaseManager.getInstance()
                .recordingArtistCreditDao().queryForEq("artist_id", artistId);
            for (RecordingArtistCredit c : credits) {
                Recording rec = c.getRecording();
                if (rec == null) continue;
                rows.add(rec);
                model.addRow(new Object[]{
                    rec.getTitle(),
                    LibraryRepository.albumForRecording(rec)
                });
            }
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected PlayQueueItem queueItemAt(int row) {
        Recording rec = rows.get(row);
        return new PlayQueueItem(rec.getPlayUri(), rec.getTitle(),
                LibraryRepository.artistNamesForRecording(rec), rec.getDurationMs(), () -> {
            if (!PlaybackCoordinator.play(rec.getIsrc(), rec.getTitle(),
                    LibraryRepository.primaryArtistForRecording(rec))) {
                PlaybackCoordinator.playUri(rec.getPlayUri());
            }
        });
    }

    @Override public boolean acceptsUri(String uri) { return uri != null && URI.matcher(uri).matches(); }
    @Override public String getTitle() { return "Artist"; }
}
