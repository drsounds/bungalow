package se.spacify.app.data.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import se.spacify.app.data.DataRepository;
import se.spacify.app.data.Format;
import se.spacify.app.data.model.DataField;
import se.spacify.app.data.model.DataRow;
import se.spacify.app.data.model.DataTable;
import se.spacify.app.data.model.DataValue;
import se.spacify.app.data.model.FieldType;
import se.spacify.app.library.views.FormDialog;
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
        jtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = jtable.rowAtPoint(e.getPoint());
                    if (row >= 0) openRow(row);
                }
            }
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
            if (r >= 0) openRow(r);
        });
        deleteBtn.getComponent().addActionListener(e -> {
            int r = jtable.getSelectedRow();
            if (r >= 0) { onDeleteRow(r); reload(); }
        });
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
        String[] names = new String[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            names[i] = fields.get(i).getName();
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
        Object[] cells = new Object[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            DataField f = fields.get(i);
            cells[i] = formatCell(f, repo.valuesFor(r.getId(), f.getId()));
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
        String[] labels = new String[fields.size()];
        JComponent[] comps = new JComponent[fields.size()];
        JTextField[] inputs = new JTextField[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            DataField f = fields.get(i);
            labels[i] = f.getName() + " (" + f.getType().name().toLowerCase() + ")";
            inputs[i] = new JTextField();
            comps[i] = inputs[i];
        }
        if (!FormDialog.show(getComponent(), "New Row", labels, comps)) return;
        Map<String, Object> posted = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            posted.put("f_" + fields.get(i).getSlug(), inputs[i].getText());
        }
        try {
            repo.createRow(dataTable, fields, posted);
        } catch (Exception e) {
            showError(e);
        }
    }

    private void onDeleteRow(int row) {
        DataRow r = rows.get(row);
        if (!confirm("this row")) return;
        try {
            repo.deleteRow(dataTable, r);
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void openRow(int row) {
        DataRow r = rows.get(row);
        getViewStack().navigate(repo.rowUri(tableSlug, r.getId()));
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
