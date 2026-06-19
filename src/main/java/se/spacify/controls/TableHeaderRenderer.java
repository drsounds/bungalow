package se.spacify.controls;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;

public class TableHeaderRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;
    private Table table;
    public Skin getSkin() {
    	return getMainWindow().getSkin();
    }

	public MainWindow getMainWindow() {
		return ((MainWindow)(SwingUtilities.getWindowAncestor(this)));
	}
	
	public TableHeaderRenderer(Table table) {
        setHorizontalAlignment(CENTER);
        this.table = table;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
    	
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
    	Graphics2D g2 = (Graphics2D)g;
    	
        super.paintComponent(g);
    }
}