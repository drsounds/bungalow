package se.spacify.plugin.library.views;

import se.spacify.db.DatabaseManager;
import se.spacify.db.LibraryRepository;
import se.spacify.db.entity.Playable;
import se.spacify.db.entity.Recording;
import se.spacify.db.entity.Release;
import se.spacify.db.entity.Track;
import se.spacify.navigation.ViewStack;
import se.spacify.plugin.playlist.models.Playlist;
import se.spacify.plugin.playlist.service.PlaylistService;
import se.spacify.service.media.PlayRequest;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Library view listing tracks joined to their recording (name, artists) and
 * release (album), with full CRUD. Also serves as the default library landing
 * view ({@code spacify:library}).
 */
public class TracksLibraryView extends AbstractMusicListView {

    public TracksLibraryView(ViewStack viewStack) {
        super(viewStack);
        //TODO Auto-generated constructor stub
    }

    private final List<Track> rows = new ArrayList<>();

    /** Track play-URI → the playlist it first appears in; rebuilt on reload. */
    private final Map<String, GroupRef> playlistByUri = new HashMap<>();

    private static final GroupRef NOT_IN_PLAYLIST =
        new GroupRef("playlist:none", "Not in a playlist", "");

    @Override protected String[] getColumns() {
        return new String[]{"#", "Recording", "Artists", "Album"};
    }

    @Override protected boolean supportsGrouping() { return true; }

    @Override
    protected List<Grouping> groupings() {
        return List.of(byRelease, byPlaylist);
    }

    /** Group tracks by the album (release) they belong to. */
    private final Grouping byRelease = new Grouping() {
        @Override public String name() { return "Release"; }
        @Override public GroupRef groupOf(int row) {
            Release r = rows.get(row).getRelease();
            if (r == null) return new GroupRef("release:none", "Unknown release", "");
            return new GroupRef("release:" + r.getId(), r.getTitle(),
                    LibraryRepository.artistNamesForRelease(r));
        }
    };

    /** Group tracks by the playlist they appear in (see {@link #playlistByUri}). */
    private final Grouping byPlaylist = new Grouping() {
        @Override public String name() { return "Playlist"; }
        @Override public GroupRef groupOf(int row) {
            String uri = rows.get(row).getPlayUri();
            GroupRef ref = uri != null ? playlistByUri.get(uri) : null;
            return ref != null ? ref : NOT_IN_PLAYLIST;
        }
    };

    @Override
    protected void reload() {
        rows.clear();
        model.setRowCount(0);
        rebuildPlaylistIndex();
        try {
            // Present albums coherently: group by release title, then within each
            // album fall back to the canonical side/track-number ascending order.
            List<Track> tracks = new ArrayList<>(DatabaseManager.getInstance().trackDao().queryForAll());
            tracks.sort(Comparator.comparing(
                    (Track t) -> t.getRelease() != null && t.getRelease().getTitle() != null
                            ? t.getRelease().getTitle() : "",
                    String.CASE_INSENSITIVE_ORDER)
                .thenComparing(LibraryRepository.ALBUM_ORDER));
            for (Track t : tracks) {
                rows.add(t);
                Recording rec = t.getRecording();
                model.addRow(new Object[]{
                    t.getTrackNumber(),
                    rec != null ? rec.getTitle() : "",
                    rec != null ? LibraryRepository.artistNamesForRecording(rec) : "",
                    t.getRelease() != null ? t.getRelease().getTitle() : ""
                });
            }
        } catch (Exception e) {
            showError(e);
        }
    }

    /**
     * Map every playlist item's play-URI to its playlist so the "Playlist"
     * grouping can place each track. A track in several playlists is attributed
     * to the first one encountered.
     */
    private void rebuildPlaylistIndex() {
        playlistByUri.clear();
        for (PlaylistService svc : getViewStack().getMainWindow().getServiceManager().getServices(PlaylistService.class)) {
            for (Playlist pl : svc.getPlaylists()) {
                GroupRef ref = new GroupRef("playlist:" + pl.getId(), pl.getName(),
                        pl.getItems().size() + " tracks");
                for (Playable item : pl.getItems()) {
                    String uri = item.getPlayUri();
                    if (uri != null) playlistByUri.putIfAbsent(uri, ref);
                }
            }
        }
    }

    @Override
    protected void onAdd() {
        try {
            List<Recording> recordings = DatabaseManager.getInstance().recordingDao().queryForAll();
            List<Release>   releases   = DatabaseManager.getInstance().releaseDao().queryForAll();
            if (recordings.isEmpty() || releases.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Add at least one recording and one release first.",
                    "Cannot add track", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JTextField number = new JTextField();
            JTextField side   = new JTextField();
            JTextField duration = new JTextField();
            JComboBox<Recording> recCombo = new JComboBox<>(recordings.toArray(new Recording[0]));
            JComboBox<Release>   relCombo = new JComboBox<>(releases.toArray(new Release[0]));
            if (!FormDialog.show(this, "New Track",
                    new String[]{"Track number", "Side", "Duration (m:ss)", "Recording", "Release"},
                    new JComponent[]{number, side, duration, recCombo, relCombo})) return;

            Track t = new Track();
            t.setTrackNumber(parseInt(number.getText()));
            t.setSide(blankToNull(side.getText()));
            t.setDurationMs(parseDuration(duration.getText()));
            t.setRecording((Recording) recCombo.getSelectedItem());
            t.setRelease((Release) relCombo.getSelectedItem());
            DatabaseManager.getInstance().trackDao().create(t);
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onEdit(int row) {
        Track t = rows.get(row);
        try {
            List<Recording> recordings = DatabaseManager.getInstance().recordingDao().queryForAll();
            List<Release>   releases   = DatabaseManager.getInstance().releaseDao().queryForAll();
            JTextField number = new JTextField(String.valueOf(t.getTrackNumber()));
            JTextField side   = new JTextField(t.getSide());
            JTextField duration = new JTextField(fmtDuration(t.getDurationMs()));
            JComboBox<Recording> recCombo = new JComboBox<>(recordings.toArray(new Recording[0]));
            JComboBox<Release>   relCombo = new JComboBox<>(releases.toArray(new Release[0]));
            selectById(recCombo, t.getRecording() != null ? t.getRecording().getId() : -1);
            selectReleaseById(relCombo, t.getRelease() != null ? t.getRelease().getId() : -1);
            if (!FormDialog.show(this, "Edit Track",
                    new String[]{"Track number", "Side", "Duration (m:ss)", "Recording", "Release"},
                    new JComponent[]{number, side, duration, recCombo, relCombo})) return;

            t.setTrackNumber(parseInt(number.getText()));
            t.setSide(blankToNull(side.getText()));
            t.setDurationMs(parseDuration(duration.getText()));
            t.setRecording((Recording) recCombo.getSelectedItem());
            t.setRelease((Release) relCombo.getSelectedItem());
            DatabaseManager.getInstance().trackDao().update(t);
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onDelete(int row) {
        Track t = rows.get(row);
        if (!confirmDelete("track #" + t.getTrackNumber())) return;
        try {
            DatabaseManager.getInstance().trackDao().delete(t);
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected PlayRequest playRequestAt(int row) {
        Track t = rows.get(row);
        Recording rec = t.getRecording();
        String isrc   = rec != null ? rec.getIsrc()  : null;
        String title  = rec != null ? rec.getTitle() : "";
        String artist = rec != null ? LibraryRepository.primaryArtistForRecording(rec) : "";
        // Carry the local Track so a remembered "Play with…" pick binds by FK.
        return new PlayRequest(t, isrc, title, artist, t.getPlayUri(), t.getDurationMs());
    }

    private static void selectById(JComboBox<Recording> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getId() == id) { combo.setSelectedIndex(i); return; }
        }
    }

    private static void selectReleaseById(JComboBox<Release> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getId() == id) { combo.setSelectedIndex(i); return; }
        }
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    @Override public boolean acceptsUri(String uri) {
        return uri != null && uri.matches("spacify:library(:tracks)?");
    }
    @Override public String getTitle() { return "Tracks"; }
}
