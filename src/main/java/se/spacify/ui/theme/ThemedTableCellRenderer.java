package se.spacify.ui.theme;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;

// ── Custom renderer ───────────────────────────────────────────────────────

public class ThemedTableCellRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 6139658182597024812L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        MainWindow mw = MainWindow.getInstance();
        Taste taste = mw.getTaste();
        Skin skin = taste.getSkin();
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (isSelected) {
            setBackground(taste.getAccentBackgroundColor());
            setForeground(taste.getAccentForegroundColor());
        } else {
            setBackground(row % 2 == 0
                ? skin.getColorValue(table, "table.alternateBackground", new Color(0, 0, 0, 11))
                : table.getBackground());
            setForeground(table.getForeground());
        }
        setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        return this;
    }
}
