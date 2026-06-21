package se.spacify.ui.theme;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import se.spacify.controls.Table;
import se.spacify.skinning.Skin;

// ── Custom renderer ───────────────────────────────────────────────────────

public class ThemedTableCellRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 6139658182597024812L;

    @Override
    public Component getTableCellRendererComponent(JTable jTable, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Table table = (Table)jTable;
        Taste taste = table.getTaste();
        Skin skin = taste.getSkin();
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (isSelected) {
            setBackground(taste.getAccentColor());
            setForeground(Color.WHITE);
        } else {
            setBackground(row % 2 == 0
                ? skin.getColorValue("alternateBackground", taste.getAlternateBackground())
                : taste.getBackground());
            setForeground(taste.getForeground());
        }
        setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        return this;
    }
}
