package se.spacify.app.data.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

import se.spacify.app.data.DataRepository;
import se.spacify.app.data.Format;
import se.spacify.app.data.model.DataField;
import se.spacify.app.data.model.DataRow;
import se.spacify.app.data.model.DataTable;
import se.spacify.app.data.model.DataValue;
import se.spacify.app.data.model.FieldType;
import se.spacify.app.library.views.FormDialog;
import se.spacify.app.music.controls.MusicTable;
import se.spacify.app.music.model.PlayableKind;
import se.spacify.app.music.model.PlayableRef;
import se.spacify.app.playlist.model.Playlist;
import se.spacify.app.playlist.service.PlaylistService;
import se.spacify.controls.ScrollPane;
import se.spacify.controls.Table;
import se.spacify.controls.ToolBar;
import se.spacify.controls.ToolButton;
import se.spacify.navigation.View;
import se.spacify.navigation.ViewStack;
import se.spacify.ui.theme.ThemeManager;
import se.spacify.ui.theme.ThemedTableCellRenderer;

/**
 * The {@code spacify:table:<slug>} screen: one custom table's row list. Presents
 * the same underlying grid {@link se.spacify.controls.Table} Library's own list
 * views render through (via {@link se.spacify.app.music.controls.MusicTable}),
 * with the toolbar-and-table shape {@link se.spacify.app.music.views.AbstractMusicListView}
 * uses. It can't extend that base directly: a table's columns are its user-defined
 * {@link DataField}s, which differ per table and change at runtime (add/delete
 * field), whereas {@code AbstractMusicListView}'s columns must be constant — so this
 * view rebuilds the table model's columns itself in {@link #reload()}.
 *
 * <p>Row detail and the related-rows view are unaffected — reached via
 * {@link DataRepository#rowUri} and still rendered by {@link DataView}.
 */
public class DataTableRowsView extends View {

    private static final Pattern URI = Pattern.compile("spacify:table:([^:]+)");

    private final DataRepository repo;
    private final JLabel headerLabel;
    private final Table table;
    private final JTable jtable;

    private String tableSlug;
    private DataTable dataTable;
    private List<DataField> fields = List.of();
    private final List<DataRow> rows = new ArrayList<>();

    public DataTableRowsView(ViewStack viewStack, DataRepository repo) {
        super(viewStack);
        this.repo = repo;
        getComponent().setOpaque(false);

        headerLabel = new JLabel();
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 18f));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        DefaultTableModel model = new DefaultTableModel(new Object[0], 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new Table(model);
        jtable = table.getComponent();
        jtable.setFillsViewportHeight(true);
        jtable.setShowGrid(false);
        jtable.setIntercellSpacing(new Dimension(0, 0));
        jtable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jtable.setDragEnabled(true);
        jtable.setTransferHandler(new RowTransferHandler());
        jtable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = jtable.rowAtPoint(e.getPoint());
                    if (row >= 0) openRow(row);
                }
            }
            @Override public void mousePressed(MouseEvent e)  { maybeShowRowMenu(e); }
            @Override public void mouseReleased(MouseEvent e) { maybeShowRowMenu(e); }
        });

        ToolBar toolbar = new ToolBar();
        ToolButton backBtn      = new ToolButton("Back to tables");
        ToolButton addBtn       = new ToolButton("Add");
        ToolButton viewBtn      = new ToolButton("View");
        ToolButton deleteBtn    = new ToolButton("Delete");
        ToolButton addFieldBtn  = new ToolButton("Add field…");
        ToolButton deleteFieldBtn = new ToolButton("Delete field…");
        ToolButton refreshBtn   = new ToolButton("Refresh");

        backBtn.getComponent().addActionListener(e -> getViewStack().navigate("spacify:table"));
        addBtn.getComponent().addActionListener(e -> { onAdd(); reload(); });
        viewBtn.getComponent().addActionListener(e -> {
            int r = jtable.getSelectedRow();
            if (r >= 0 && jtable.getSelectedRowCount() == 1) openRow(r);
        });
        deleteBtn.getComponent().addActionListener(e -> deleteSelected());
        addFieldBtn.getComponent().addActionListener(e -> { onAddField(); reload(); });
        deleteFieldBtn.getComponent().addActionListener(e -> { onDeleteField(); reload(); });
        refreshBtn.getComponent().addActionListener(e -> reload());

        toolbar.add(backBtn);
        toolbar.getComponent().addSeparator();
        toolbar.add(addBtn);
        toolbar.add(viewBtn);
        toolbar.add(deleteBtn);
        toolbar.getComponent().addSeparator();
        toolbar.add(addFieldBtn);
        toolbar.add(deleteFieldBtn);
        toolbar.getComponent().addSeparator();
        toolbar.add(refreshBtn);

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.PAGE_AXIS));
        north.setOpaque(false);
        north.add(headerLabel);
        north.add(toolbar.getComponent());

        ScrollPane scroll = new ScrollPane(table);
        scroll.getComponent().setBorder(BorderFactory.createEmptyBorder());

        getComponent().add(north, BorderLayout.NORTH);
        getComponent().add(scroll.getComponent(), BorderLayout.CENTER);

        updateColors();
        ThemeManager.addChangeListener(this::updateColors);
    }

    // ── Reload / columns ─────────────────────────────────────────────────────────

    private void reload() {
        rows.clear();
        try {
            dataTable = repo.findTable(tableSlug);
            if (dataTable == null) {
                headerLabel.setText("Table not found");
                fields = List.of();
                setColumns(fields);
                return;
            }
            headerLabel.setText(dataTable.getName());
            fields = repo.listFields(dataTable);
            setColumns(fields);
            for (DataRow r : repo.listRows(dataTable)) {
                rows.add(r);
                addRowToTable(r);
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    /** Rebuild the model's columns from the table's current fields (they vary per table). */
    private void setColumns(List<DataField> fields) {
        String[] names = new String[fields.size() + 1];
        names[0] = "Name";
        for (int i = 0; i < fields.size(); i++) {
            names[i + 1] = fields.get(i).getName();
        }
        DefaultTableModel model = new DefaultTableModel(names, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        jtable.setModel(model);
        ThemedTableCellRenderer renderer = new ThemedTableCellRenderer();
        for (int i = 0; i < jtable.getColumnCount(); i++) {
            jtable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private void addRowToTable(DataRow r) throws SQLException {
        Object[] cells = new Object[fields.size() + 1];
        cells[0] = r.getName() == null ? "" : r.getName();
        for (int i = 0; i < fields.size(); i++) {
            DataField f = fields.get(i);
            cells[i + 1] = formatCell(f, repo.valuesFor(r.getId(), f.getId()));
        }
        ((DefaultTableModel) jtable.getModel()).addRow(cells);
    }

    private static String formatCell(DataField f, List<DataValue> values) {
        if (values.isEmpty()) return "";
        if (f.getType() == FieldType.LINK) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(values.get(i).getValue());
            }
            return sb.toString();
        }
        String raw = values.get(0).getValue();
        try {
            return switch (f.getType()) {
                case NUMBER -> Format.number(Long.parseLong(raw));
                case FLOAT -> Format.decimal(Double.parseDouble(raw));
                case TIMESTAMP -> Format.timestamp(Long.parseLong(raw));
                case TEXT, LINK -> raw;
            };
        } catch (NumberFormatException e) {
            return raw;
        }
    }

    // ── Row actions ──────────────────────────────────────────────────────────────

    private void onAdd() {
        if (dataTable == null) return;
        String[] labels = new String[fields.size() + 1];
        JComponent[] comps = new JComponent[fields.size() + 1];
        JTextField[] inputs = new JTextField[fields.size() + 1];
        labels[0] = "Name";
        inputs[0] = new JTextField();
        comps[0] = inputs[0];
        for (int i = 0; i < fields.size(); i++) {
            DataField f = fields.get(i);
            labels[i + 1] = f.getName() + " (" + f.getType().name().toLowerCase() + ")";
            inputs[i + 1] = new JTextField();
            comps[i + 1] = inputs[i + 1];
        }
        if (!FormDialog.show(getComponent(), "New Row", labels, comps)) return;
        Map<String, Object> posted = new HashMap<>();
        posted.put("name", inputs[0].getText());
        for (int i = 0; i < fields.size(); i++) {
            posted.put("f_" + fields.get(i).getSlug(), inputs[i + 1].getText());
        }
        try {
            repo.createRow(dataTable, fields, posted);
        } catch (Exception e) {
            showError(e);
        }
    }

    private void openRow(int row) {
        DataRow r = rows.get(row);
        getViewStack().navigate(repo.rowUri(tableSlug, r.getId()));
    }

    /** The rows currently selected in the grid, in view order. */
    private List<DataRow> selectedRows() {
        List<DataRow> out = new ArrayList<>();
        for (int r : jtable.getSelectedRows()) {
            out.add(rows.get(r));
        }
        return out;
    }

    /** Delete every selected row after one confirmation covering the whole selection. */
    private void deleteSelected() {
        List<DataRow> toDelete = selectedRows();
        if (toDelete.isEmpty()) return;
        String what = toDelete.size() == 1 ? "this row" : toDelete.size() + " rows";
        if (!confirm(what)) return;
        try {
            for (DataRow r : toDelete) {
                repo.deleteRow(dataTable, r);
            }
        } catch (SQLException e) {
            showError(e);
        }
        reload();
    }

    /** Wrap a row as a {@link PlayableRef} — the drag payload and "Add to playlist" item shape. */
    private PlayableRef playableFor(DataRow r) {
        String name = r.getName() != null ? r.getName() : dataTable.getName() + " row";
        return new PlayableRef(PlayableKind.TRACK, repo.rowUri(tableSlug, r.getId()), name, null, 0L, List.of());
    }

    private void addSelectedToPlaylist() {
        List<DataRow> selected = selectedRows();
        if (selected.isEmpty()) return;
        PlaylistService svc = editablePlaylistService();
        if (svc == null) {
            JOptionPane.showMessageDialog(getComponent(), "No editable playlist is available.",
                    "Add to playlist", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        final String NEW = "＋ New playlist…";
        List<Object> options = new ArrayList<>(svc.getPlaylists());
        options.add(NEW);
        Object choice = JOptionPane.showInputDialog(getComponent(), "Add to playlist:", "Add to playlist",
                JOptionPane.PLAIN_MESSAGE, null, options.toArray(), options.get(0));
        if (choice == null) return;
        try {
            Playlist target;
            if (NEW.equals(choice)) {
                String name = JOptionPane.showInputDialog(getComponent(), "Playlist name:", "New Playlist",
                        JOptionPane.PLAIN_MESSAGE);
                if (name == null || name.isBlank()) return;
                target = svc.createPlaylist(name.trim());
            } else {
                target = (Playlist) choice;
            }
            for (DataRow r : selected) {
                svc.addToPlaylist(target.getPublicId(), playableFor(r), PlayableKind.TRACK.id());
            }
        } catch (Exception e) {
            showError(e);
        }
    }

    private PlaylistService editablePlaylistService() {
        for (PlaylistService svc : getViewStack().getMainWindow().getServiceManager().getServices(PlaylistService.class)) {
            if (svc.isEditable()) return svc;
        }
        return null;
    }

    /**
     * Right-click row menu: View row (single selection only), Add to playlist, Delete —
     * the CRUD-relevant subset that makes sense on an existing row (Create stays a
     * toolbar-only "Add"; editing already happens by opening the row's detail page).
     * Right-clicking a row already inside the current selection leaves that selection
     * alone so the menu acts on it all; right-clicking outside it collapses to just
     * that row, matching typical list/file-manager behavior.
     */
    private void maybeShowRowMenu(MouseEvent e) {
        if (!e.isPopupTrigger()) return;
        int row = jtable.rowAtPoint(e.getPoint());
        if (row < 0) return;
        if (!jtable.isRowSelected(row)) {
            jtable.setRowSelectionInterval(row, row);
        }
        int count = jtable.getSelectedRowCount();

        JPopupMenu menu = new JPopupMenu();
        JMenuItem view = new JMenuItem("View row");
        view.setEnabled(count == 1);
        view.addActionListener(a -> openRow(row));
        menu.add(view);

        JMenuItem addToPlaylist = new JMenuItem("Add to playlist");
        addToPlaylist.addActionListener(a -> addSelectedToPlaylist());
        menu.add(addToPlaylist);

        menu.addSeparator();
        JMenuItem delete = new JMenuItem(count > 1 ? "Delete " + count + " rows" : "Delete");
        delete.addActionListener(a -> deleteSelected());
        menu.add(delete);

        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Drag export for a row: carries both {@link MusicTable#PLAYABLE_REF_FLAVOR} (so an
     * existing playlist view or the sidebar's playlist tree can accept the drop today,
     * unchanged) and a plain {@link DataFlavor#stringFlavor} of the row's own
     * {@code spacify:table:<slug>:<rowId>} URI, for any plain-text drop target. Exports
     * only the drag anchor row, independent of a wider multi-selection — {@link MusicTable}
     * itself only ever drags one row too.
     */
    private final class RowTransferHandler extends TransferHandler {
        @Override public int getSourceActions(JComponent c) { return COPY; }

        @Override protected Transferable createTransferable(JComponent c) {
            int row = jtable.getSelectedRow();
            if (row < 0) return null;
            PlayableRef ref = playableFor(rows.get(row));
            return new RowTransferable(new MusicTable.PlaylistDrag(ref, false), ref.getPlayUri());
        }
    }

    private record RowTransferable(MusicTable.PlaylistDrag drag, String uri) implements Transferable {
        @Override public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { MusicTable.PLAYABLE_REF_FLAVOR, DataFlavor.stringFlavor };
        }
        @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
            return MusicTable.PLAYABLE_REF_FLAVOR.equals(flavor) || DataFlavor.stringFlavor.equals(flavor);
        }
        @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (MusicTable.PLAYABLE_REF_FLAVOR.equals(flavor)) return drag;
            if (DataFlavor.stringFlavor.equals(flavor)) return uri;
            throw new UnsupportedFlavorException(flavor);
        }
    }

    // ── Field management ─────────────────────────────────────────────────────────

    private void onAddField() {
        if (dataTable == null) return;
        JTextField name = new JTextField();
        JComboBox<FieldType> type = new JComboBox<>(FieldType.values());
        if (!FormDialog.show(getComponent(), "Add Field", new String[]{"Name", "Type"},
                new JComponent[]{name, type})) return;
        try {
            repo.addField(dataTable, name.getText(), ((FieldType) type.getSelectedItem()).name());
        } catch (Exception e) {
            showError(e);
        }
    }

    private void onDeleteField() {
        if (dataTable == null || fields.isEmpty()) return;
        JComboBox<DataField> chooser = new JComboBox<>(fields.toArray(new DataField[0]));
        chooser.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean hasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                if (value instanceof DataField f) setText(f.getName() + " (" + f.getType().name().toLowerCase() + ")");
                return this;
            }
        });
        if (!FormDialog.show(getComponent(), "Delete Field", new String[]{"Field"}, new JComponent[]{chooser})) return;
        DataField f = (DataField) chooser.getSelectedItem();
        if (f == null || !confirm("field \"" + f.getName() + "\"")) return;
        try {
            repo.deleteField(f);
        } catch (SQLException e) {
            showError(e);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private boolean confirm(String what) {
        return JOptionPane.showConfirmDialog(getComponent(), "Delete " + what + "?", "Confirm delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(getComponent(), e.getMessage(), "Custom Tables error", JOptionPane.ERROR_MESSAGE);
    }

    private void updateColors() {
        headerLabel.setForeground(ThemeManager.getForeground());
        jtable.setBackground(ThemeManager.getBackground());
        jtable.setForeground(ThemeManager.getForeground());
        jtable.setGridColor(ThemeManager.getGridColor());
    }

    // ── View ─────────────────────────────────────────────────────────────────────

    @Override public boolean acceptsUri(String uri) { return uri != null && URI.matcher(uri).matches(); }

    @Override
    public void navigate(String uri) {
        Matcher m = URI.matcher(uri);
        tableSlug = m.matches() ? m.group(1) : null;
    }

    @Override public void onShow() { reload(); }

    @Override public String getName() { return dataTable != null ? dataTable.getName() : "Table"; }
}
