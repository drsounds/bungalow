package se.spacify.app.data.views;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextField;

import se.spacify.app.data.DataRepository;
import se.spacify.app.data.Format;
import se.spacify.app.data.model.DataTable;
import se.spacify.app.library.views.FormDialog;
import se.spacify.app.music.controls.MusicTable;
import se.spacify.app.music.views.AbstractMusicListView;
import se.spacify.navigation.ViewStack;

/**
 * The {@code spacify:table} screen: the index of custom tables, presented with the
 * same {@link AbstractMusicListView}/{@link MusicTable} grid Library's own list
 * views (see {@code se.spacify.app.library.views}) use — same table control, same
 * toolbar shape, same double-click-to-open interaction. Rows aren't playable, so
 * the Buy/Stream column never shows; {@link #showsScan()} hides the (Library-only)
 * "Scan…" button.
 */
public class DataTablesListView extends AbstractMusicListView {

    private final DataRepository repo;
    private final List<DataTable> rows = new ArrayList<>();

    public DataTablesListView(ViewStack viewStack, DataRepository repo) {
        super(viewStack);
        this.repo = repo;
    }

    @Override protected boolean showsScan() { return false; }

    @Override protected List<MusicTable.Column> getColumns() {
        return List.of(
            column("name",    "Name"),
            column("slug",    "Slug"),
            column("fields",  "Fields"),
            column("rows",    "Rows"),
            column("updated", "Updated"));
    }

    @Override
    protected void reload() {
        rows.clear();
        musicTable.clear();
        try {
            for (DataTable t : repo.listTables()) {
                rows.add(t);
                addRow(row()
                    .set("name",    t.getName())
                    .set("slug",    t.getSlug())
                    .set("fields",  repo.listFields(t).size())
                    .set("rows",    repo.listRows(t).size())
                    .set("updated", Format.timestamp(t.getUpdatedAt())));
            }
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onAdd() {
        JTextField name = new JTextField();
        if (!FormDialog.show(getComponent(), "New Table", new String[]{"Name"}, new JComponent[]{name})) return;
        if (name.getText().isBlank()) return;
        try {
            repo.createTable(name.getText().trim());
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onEdit(int row) {
        DataTable t = rows.get(row);
        JTextField name = new JTextField(t.getName());
        if (!FormDialog.show(getComponent(), "Rename Table", new String[]{"Name"}, new JComponent[]{name})) return;
        if (name.getText().isBlank()) return;
        try {
            repo.renameTable(t, name.getText().trim());
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onDelete(int row) {
        DataTable t = rows.get(row);
        if (!confirmDelete("table \"" + t.getName() + "\"")) return;
        try {
            repo.deleteTable(t);
        } catch (Exception e) {
            showError(e);
        }
    }

    @Override
    protected void onActivate(int row) {
        DataTable t = rows.get(row);
        getViewStack().navigate(repo.tableUri(t.getSlug()));
    }

    @Override public boolean acceptsUri(String uri) { return "spacify:table".equals(uri); }
    @Override public String getName() { return "Custom Tables"; }
}
