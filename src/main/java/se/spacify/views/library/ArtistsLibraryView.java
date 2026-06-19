package se.spacify.views.library;

import se.spacify.db.DatabaseManager;
import se.spacify.db.entity.Artist;
import se.spacify.navigation.SPViewStack;
import se.spacify.ui.MainWindow;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/** Library view listing artists (name, ISNI, MBID) with full CRUD. */
public class ArtistsLibraryView extends AbstractLibraryView {

    public ArtistsLibraryView(SPViewStack viewStack) {
        super(viewStack);
        //TODO Auto-generated constructor stub
    }
    private final List<Artist> rows = new ArrayList<>();

    @Override protected String[] getColumns() { return new String[]{"Name", "ISNI", "MBID"}; }

    @Override
    protected void reload() {
        rows.clear();
        model.setRowCount(0);
        try {
            for (Artist a : DatabaseManager.getInstance().artistDao().queryForAll()) {
                rows.add(a);
                model.addRow(new Object[]{a.getName(), a.getIsni(), a.getMbid()});
            }
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onAdd() {
        JTextField name = new JTextField();
        JTextField isni = new JTextField();
        JTextField mbid = new JTextField();
        if (!FormDialog.show(panel, "New Artist",
                new String[]{"Name", "ISNI", "MBID"},
                new JComponent[]{name, isni, mbid})) return;
        if (name.getText().isBlank()) return;
        try {
            Artist a = new Artist(name.getText().trim());
            a.setIsni(blankToNull(isni.getText()));
            a.setMbid(blankToNull(mbid.getText()));
            DatabaseManager.getInstance().artistDao().create(a);
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onEdit(int row) {
        Artist a = rows.get(row);
        JTextField name = new JTextField(a.getName());
        JTextField isni = new JTextField(a.getIsni());
        JTextField mbid = new JTextField(a.getMbid());
        if (!FormDialog.show(panel, "Edit Artist",
                new String[]{"Name", "ISNI", "MBID"},
                new JComponent[]{name, isni, mbid})) return;
        if (name.getText().isBlank()) return;
        try {
            a.setName(name.getText().trim());
            a.setIsni(blankToNull(isni.getText()));
            a.setMbid(blankToNull(mbid.getText()));
            DatabaseManager.getInstance().artistDao().update(a);
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onDelete(int row) {
        Artist a = rows.get(row);
        if (!confirmDelete("artist \"" + a.getName() + "\"")) return;
        try {
            DatabaseManager.getInstance().artistDao().delete(a);
        } catch (Exception e) {
            showError(e);
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    @Override public boolean acceptsUri(String uri) { return "spacify:library:artists".equals(uri); }
    @Override public String getTitle() { return "Artists"; }
}
