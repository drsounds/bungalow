package se.spacify.app.library.views;
import se.spacify.app.music.views.AbstractMusicListView;

import se.spacify.db.DatabaseManager;
import se.spacify.db.LibraryRepository;
import se.spacify.db.entity.Recording;
import se.spacify.navigation.ViewStack;
import se.spacify.service.media.PlayRequest;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/** Library view listing recordings (name, artists) with full CRUD. */
public class RecordingsLibraryView extends AbstractMusicListView {

    public RecordingsLibraryView(ViewStack viewStack) {
        super(viewStack);
        //TODO Auto-generated constructor stub
    }
    private final List<Recording> rows = new ArrayList<>();

    @Override protected String[] getColumns() {
        return new String[]{"Name", "Artists", "ISRC", "Duration"};
    }

    @Override
    protected void reload() {
        rows.clear();
        model.setRowCount(0);
        try {
            for (Recording r : DatabaseManager.getInstance().recordingDao().queryForAll()) {
                rows.add(r);
                model.addRow(new Object[]{
                    r.getTitle(),
                    LibraryRepository.artistNamesForRecording(r),
                    r.getIsrc(),
                    fmtDuration(r.getDurationMs())
                });
            }
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onAdd() {
        JTextField title    = new JTextField();
        JTextField artists  = new JTextField();
        JTextField isrc     = new JTextField();
        JTextField duration = new JTextField();
        JTextField filePath = new JTextField();
        if (!FormDialog.show(getComponent(), "New Recording",
                new String[]{"Title", "Artists (comma-separated)", "ISRC", "Duration (m:ss)", "File path"},
                new JComponent[]{title, artists, isrc, duration, filePath})) return;
        if (title.getText().isBlank()) return;
        try {
            Recording r = new Recording(title.getText().trim());
            r.setIsrc(blankToNull(isrc.getText()));
            r.setDurationMs(parseDuration(duration.getText()));
            r.setFilePath(blankToNull(filePath.getText()));
            DatabaseManager.getInstance().recordingDao().create(r);
            LibraryRepository.setRecordingArtists(r, LibraryRepository.parseArtistNames(artists.getText()));
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onEdit(int row) {
        Recording r = rows.get(row);
        JTextField title    = new JTextField(r.getTitle());
        JTextField artists  = new JTextField(LibraryRepository.artistNamesForRecording(r));
        JTextField isrc     = new JTextField(r.getIsrc());
        JTextField duration = new JTextField(fmtDuration(r.getDurationMs()));
        JTextField filePath = new JTextField(r.getFilePath());
        if (!FormDialog.show(getComponent(), "Edit Recording",
                new String[]{"Title", "Artists (comma-separated)", "ISRC", "Duration (m:ss)", "File path"},
                new JComponent[]{title, artists, isrc, duration, filePath})) return;
        if (title.getText().isBlank()) return;
        try {
            r.setTitle(title.getText().trim());
            r.setIsrc(blankToNull(isrc.getText()));
            r.setDurationMs(parseDuration(duration.getText()));
            r.setFilePath(blankToNull(filePath.getText()));
            DatabaseManager.getInstance().recordingDao().update(r);
            LibraryRepository.setRecordingArtists(r, LibraryRepository.parseArtistNames(artists.getText()));
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onDelete(int row) {
        Recording r = rows.get(row);
        if (!confirmDelete("recording \"" + r.getTitle() + "\"")) return;
        try {
            DatabaseManager.getInstance().recordingArtistCreditDao().delete(
                DatabaseManager.getInstance().recordingArtistCreditDao().queryForEq("recording_id", r.getId()));
            DatabaseManager.getInstance().recordingDao().delete(r);
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected PlayRequest playRequestAt(int row) {
        Recording r = rows.get(row);
        // No local Track here; the saved "Play with…" pick is keyed by ISRC/URI.
        return new PlayRequest(null, r.getIsrc(), r.getTitle(),
                LibraryRepository.primaryArtistForRecording(r), r.getPlayUri(), r.getDurationMs());
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    @Override public boolean acceptsUri(String uri) { return "spacify:library:recordings".equals(uri); }
    @Override public String getTitle() { return "Recordings"; }
}
