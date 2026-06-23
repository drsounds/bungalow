package se.spacify.app.library.views;

import se.spacify.controls.Table;
import se.spacify.navigation.View;
import se.spacify.navigation.ViewStack;
import se.spacify.ui.theme.ThemeManager;
import se.spacify.ui.theme.ThemedTableCellRenderer;

import javax.swing.*;
import java.awt.*;

public class LibraryView extends View {

    private final Table     table;
    private final JScrollPane scroll;

    public LibraryView(ViewStack viewStack) {
        super(viewStack);
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        String[] columns = {"Title", "Artist", "Album"};
        Object[][] data = {
        		
        };

        table = new Table(data, columns);
        table.setFillsViewportHeight(true);
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

        add(scroll, BorderLayout.CENTER);

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

    @Override public boolean acceptsUri(String uri) { return uri != null && uri.matches("spacify:library.*"); }
    @Override public void navigate(String uri) {}
    @Override public String getTitle() { return "Your Library"; }
}
