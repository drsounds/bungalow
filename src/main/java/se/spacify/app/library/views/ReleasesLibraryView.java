package se.spacify.app.library.views;
import se.spacify.app.music.controls.MusicTable;
import se.spacify.app.music.views.AbstractMusicListView;

import se.spacify.db.DatabaseManager;
import se.spacify.db.LibraryRepository;
import se.spacify.db.entity.Release;
import se.spacify.db.entity.Release.ReleaseType;
import se.spacify.navigation.ViewStack;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/** Library view listing releases (name, artists, type, date) with full CRUD. */
public class ReleasesLibraryView extends AbstractMusicListView {

    public ReleasesLibraryView(ViewStack viewStack) {
        super(viewStack);
        //TODO Auto-generated constructor stub
    }
    private final List<Release> rows = new ArrayList<>();

    @Override protected List<MusicTable.Column> getColumns() {
        return List.of(
            column("name",    "Name"),
            column("artists", "Artists"),
            column("type",    "Type"),
            column("date",    "Date"));
    }

    @Override
    protected void reload() {
        rows.clear();
        musicTable.clear();
        try {
            for (Release r : DatabaseManager.getInstance().releaseDao().queryForAll()) {
                rows.add(r);
                addRow(row()
                    .set("name",    r.getTitle())
                    .set("artists", LibraryRepository.artistNamesForRelease(r))
                    .set("type",    r.getType() != null ? r.getType().name() : "")
                    .set("date",    r.getReleaseDate()));
            }
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onAdd() {
        JTextField title   = new JTextField();
        JTextField artists = new JTextField();
        JComboBox<ReleaseType> type = new JComboBox<>(ReleaseType.values());
        JTextField date    = new JTextField();
        JTextField upc     = new JTextField();
        JTextField mbid    = new JTextField();
        if (!FormDialog.show(getComponent(), "New Release",
                new String[]{"Title", "Artists (comma-separated)", "Type", "Date (YYYY-MM-DD)", "UPC", "MBID"},
                new JComponent[]{title, artists, type, date, upc, mbid})) return;
        if (title.getText().isBlank()) return;
        try {
            Release r = new Release(title.getText().trim());
            r.setType((ReleaseType) type.getSelectedItem());
            r.setReleaseDate(blankToNull(date.getText()));
            r.setUpc(blankToNull(upc.getText()));
            r.setMbid(blankToNull(mbid.getText()));
            DatabaseManager.getInstance().releaseDao().create(r);
            LibraryRepository.setReleaseArtists(r, LibraryRepository.parseArtistNames(artists.getText()));
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onEdit(int row) {
        Release r = rows.get(row);
        JTextField title   = new JTextField(r.getTitle());
        JTextField artists = new JTextField(LibraryRepository.artistNamesForRelease(r));
        JComboBox<ReleaseType> type = new JComboBox<>(ReleaseType.values());
        if (r.getType() != null) type.setSelectedItem(r.getType());
        JTextField date    = new JTextField(r.getReleaseDate());
        JTextField upc     = new JTextField(r.getUpc());
        JTextField mbid    = new JTextField(r.getMbid());
        if (!FormDialog.show(getComponent(), "Edit Release",
                new String[]{"Title", "Artists (comma-separated)", "Type", "Date (YYYY-MM-DD)", "UPC", "MBID"},
                new JComponent[]{title, artists, type, date, upc, mbid})) return;
        if (title.getText().isBlank()) return;
        try {
            r.setTitle(title.getText().trim());
            r.setType((ReleaseType) type.getSelectedItem());
            r.setReleaseDate(blankToNull(date.getText()));
            r.setUpc(blankToNull(upc.getText()));
            r.setMbid(blankToNull(mbid.getText()));
            DatabaseManager.getInstance().releaseDao().update(r);
            LibraryRepository.setReleaseArtists(r, LibraryRepository.parseArtistNames(artists.getText()));
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onDelete(int row) {
        Release r = rows.get(row);
        if (!confirmDelete("release \"" + r.getTitle() + "\"")) return;
        try {
            DatabaseManager.getInstance().releaseArtistCreditDao().delete(
                DatabaseManager.getInstance().releaseArtistCreditDao().queryForEq("release_id", r.getId()));
            DatabaseManager.getInstance().releaseDao().delete(r);
        } catch (Exception e) {
            showError(e);
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    @Override public boolean acceptsUri(String uri) { return "spacify:library:releases".equals(uri); }
    @Override public String getTitle() { return "Releases"; }
}
