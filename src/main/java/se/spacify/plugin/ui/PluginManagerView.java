package se.spacify.plugin.ui;

import se.spacify.controls.Table;
import se.spacify.navigation.SPView;
import se.spacify.navigation.SPViewStack;
import se.spacify.plugin.PluginDescriptor;
import se.spacify.plugin.PluginManager;
import se.spacify.plugin.PluginManager.ManagedPlugin;
import se.spacify.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Plugin manager, reachable at {@code spacify:plugins}. Lists every discovered
 * plugin with an enable/disable checkbox, supports installing external jars
 * (copied into {@code ~/Bungalow}) and removing them, and shows an
 * auto-generated settings editor for the selected plugin.
 */
public class PluginManagerView extends SPView {

    private final JPanel           panel;
    private final Table           table;
    private final DefaultTableModel model;
    private final JScrollPane      detail;
    private final List<ManagedPlugin> rows = new ArrayList<>();

    private boolean refreshing = false;

    public PluginManagerView(SPViewStack viewStack) {
        super(viewStack);
        panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(true);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ── Header + toolbar ─────────────────────────────────────────────────
        JLabel title = new JLabel("Plugins");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JButton addBtn    = new JButton("Add…");
        JButton removeBtn = new JButton("Remove");
        addBtn.addActionListener(e -> onAdd());
        removeBtn.addActionListener(e -> onRemove());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.setOpaque(false);
        buttons.add(addBtn);
        buttons.add(removeBtn);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(buttons, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        // ── Plugin table ─────────────────────────────────────────────────────
        model = new DefaultTableModel(new String[]{"On", "Name", "Version", "Source"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 0; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
        };
        table = new Table(model);
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.getColumnModel().getColumn(0).setMaxWidth(32);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) showSelected(); });
        model.addTableModelListener(e -> {
            if (refreshing || e.getColumn() != 0) return;
            int r = e.getFirstRow();
            if (r >= 0 && r < rows.size()) {
                boolean on = Boolean.TRUE.equals(model.getValueAt(r, 0));
                getViewStack().getMainWindow().getPluginManager().setEnabled(rows.get(r).getDescriptor().getId(), on);
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());

        detail = new JScrollPane();
        detail.setBorder(BorderFactory.createEmptyBorder());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, detail);
        split.setDividerLocation(360);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setOpaque(false);
        panel.add(split, BorderLayout.CENTER);

        refresh();
        applyTheme();
        getViewStack().getMainWindow().getPluginManager().addChangeListener(() -> SwingUtilities.invokeLater(this::refresh));
        getViewStack().getMainWindow().getThemeManager().addChangeListener(this::applyTheme);
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void onAdd() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Plugin jar (*.jar)", "jar"));
        if (chooser.showOpenDialog(panel) != JFileChooser.APPROVE_OPTION) return;
        File jar = chooser.getSelectedFile();
        boolean ok = getViewStack().getMainWindow().getPluginManager().install(jar);
        if (!ok) {
            JOptionPane.showMessageDialog(panel,
                "Not a valid Spacify plugin jar (missing Spacify-Plugin-Id / -Class manifest headers).",
                "Install failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRemove() {
        ManagedPlugin m = selected();
        if (m == null) return;
        if (!m.getDescriptor().isRemovable()) {
            JOptionPane.showMessageDialog(panel,
                "Built-in plugins can't be removed — disable it with the checkbox instead.",
                "Remove", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int ans = JOptionPane.showConfirmDialog(panel,
            "Remove plugin \"" + m.getDescriptor().getName() + "\"?",
            "Remove plugin", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans == JOptionPane.YES_OPTION) {
            getViewStack().getMainWindow().getPluginManager().uninstall(m.getDescriptor().getId());
        }
    }

    // ── Table / detail rendering ─────────────────────────────────────────────

    private void refresh() {
        String selectedId = selected() != null ? selected().getDescriptor().getId() : null;

        refreshing = true;
        rows.clear();
        model.setRowCount(0);
        for (ManagedPlugin m : PluginManager.getInstance().getPlugins()) {
            rows.add(m);
            PluginDescriptor d = m.getDescriptor();
            model.addRow(new Object[]{ m.isEnabled(), d.getName(), d.getVersion(), sourceLabel(d.getSource()) });
        }
        refreshing = false;

        // Restore selection by id.
        int sel = -1;
        for (int i = 0; i < rows.size(); i++)
            if (rows.get(i).getDescriptor().getId().equals(selectedId)) { sel = i; break; }
        if (sel < 0 && !rows.isEmpty()) sel = 0;
        if (sel >= 0) table.setRowSelectionInterval(sel, sel);
        else detail.setViewportView(null);
        applyTheme();
    }

    private void showSelected() {
        ManagedPlugin m = selected();
        if (m == null) { detail.setViewportView(null); return; }

        JPanel info = new JPanel(new BorderLayout(0, 8));
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 4));

        PluginDescriptor d = m.getDescriptor();
        JLabel head = new JLabel("<html><b>" + d.getName() + "</b> " + d.getVersion()
            + "<br><span style='font-size:9px'>" + d.getId() + " — "
            + sourceLabel(d.getSource()) + (m.isActive() ? " — active" : "") + "</span></html>");
        head.setForeground(ThemeManager.getForeground());
        if (m.getIcon() != null) head.setIcon(m.getIcon());
        info.add(head, BorderLayout.NORTH);
        info.add(new PluginSettingsEditor(m), BorderLayout.CENTER);

        detail.setViewportView(info);
    }

    private ManagedPlugin selected() {
        int r = table.getSelectedRow();
        return (r >= 0 && r < rows.size()) ? rows.get(r) : null;
    }

    private static String sourceLabel(PluginDescriptor.Source s) {
        return switch (s) {
            case BUILTIN_BUNDLE -> "built-in";
            case APP_DIR        -> "bundled";
            case EXTERNAL       -> "external";
        };
    }

    private void applyTheme() {
        Color bg = ThemeManager.getBackground();
        Color fg = ThemeManager.getForeground();
        panel.setBackground(bg);
        table.setBackground(bg);
        table.setForeground(fg);
        table.setGridColor(ThemeManager.getGridColor());
        table.setSelectionBackground(ThemeManager.getAccentColor());
        table.setSelectionForeground(Color.WHITE);
        detail.getViewport().setBackground(bg);
        detail.setBackground(bg);
        panel.repaint();
    }

    // ── SPView ───────────────────────────────────────────────────────────────

    @Override public boolean acceptsUri(String uri) { return "spacify:plugins".equals(uri); }
    @Override public void navigate(String uri) {}
    @Override public JComponent getComponent() { return panel; }
    @Override public String getTitle() { return "Plugins"; }
    @Override public void onShow() { refresh(); }
}
