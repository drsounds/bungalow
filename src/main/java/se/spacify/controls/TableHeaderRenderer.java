package se.spacify.controls;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;

import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;

public class TableHeaderRenderer extends Label implements TableCellRenderer {
    private static final long serialVersionUID = 1L;
    private Table table;
    public Table getTable() {
        return table;
    }

    public Skin getSkin() {
    	return getMainWindow().getSkin();
    }

	public MainWindow getMainWindow() {
		return ((MainWindow)(SwingUtilities.getWindowAncestor(this)));
	}
	
	public TableHeaderRenderer(Table table) { 
        super();
        this.table = table;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
    	
        setText(value.toString());

        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
    	Graphics2D g2 = (Graphics2D)g;

    	getTable().getSkin().paintTableHeader(table, getWidth(), getHeight(), g2);
        System.out.println("tableHeaderRenderer");
        super.paintComponent(g);
    }
}