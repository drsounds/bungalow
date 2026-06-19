package se.spacify.views;

import se.spacify.controls.Table;
import se.spacify.navigation.SPView;
import se.spacify.navigation.SPViewStack;
import se.spacify.ui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class LibraryView extends SPView {

    private final JPanel     panel;
    private final Table     table;
    private final JScrollPane scroll;

    public LibraryView(SPViewStack viewStack) {
        super(viewStack);
        panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        String[] columns = {"Title", "Artist", "Album"};
        Object[][] data = {
        		
        };

        table = new Table(data, columns);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setOpaque(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        ThemedTableCellRenderer renderer = new ThemedTableCellRenderer();
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);

        scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(true);
        scroll.getViewport().setOpaque(true);

        panel.add(scroll, BorderLayout.CENTER);

        updateColors();
        ThemeManager.addChangeListener(this::updateColors);
    }

    private void updateColors() {
        Color bg   = ThemeManager.getBackground();
        Color fg   = ThemeManager.getForeground();
        Color grid = ThemeManager.getGridColor();

        table.setBackground(bg);
        table.setForeground(fg);
        table.setGridColor(grid);
        scroll.setBackground(bg);
        scroll.getViewport().setBackground(bg);
        table.repaint();
    }

    // ── Custom renderer ───────────────────────────────────────────────────────

    private static final class ThemedTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 6139658182597024812L;

		@Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (isSelected) {
                setBackground(ThemeManager.getAccentColor());
                setForeground(Color.WHITE);
            } else {
                setBackground(row % 2 == 0
                    ? ThemeManager.getBackground()
                    : ThemeManager.getAlternateBackground());
                setForeground(ThemeManager.getForeground());
            }
            setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            return this;
        }
    }

    @Override public boolean acceptsUri(String uri) { return uri != null && uri.matches("spacify:library.*"); }
    @Override public void navigate(String uri) {}
    @Override public JComponent getComponent() { return panel; }
    @Override public String getTitle() { return "Your Library"; }
}
