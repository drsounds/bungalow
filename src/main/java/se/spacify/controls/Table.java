package se.spacify.controls;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;
import se.spacify.ui.theme.Theme;

public class Table extends JTable implements Control {
	
	public Skin getSkin() {
		return getMainWindow().getSkin();
	}
	private static final long serialVersionUID = 1L;
	public MainWindow getMainWindow() {
		return ((MainWindow)(SwingUtilities.getWindowAncestor(this)));
	}
	public Table() {
		this.getTableHeader().setDefaultRenderer(new TableHeaderRenderer(this));
	}
	public Table(Object[][] data, String[] columns) {
		super(data, columns);
	}
	public Table(TableModel model) {
		super(model);
		this.getTableHeader().setDefaultRenderer(new TableHeaderRenderer(this));
	}
	@Override
	public Theme getTheme() {
		// TODO Auto-generated method stub
		return getMainWindow().getTheme();
	}
}
