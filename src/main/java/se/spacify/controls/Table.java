package se.spacify.controls;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import se.spacify.design.Design;
import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;
import se.spacify.ui.theme.Taste;
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

	private Theme theme;
	public Theme getTheme() {
		if (theme != null) {
			return theme;
		}
		if (getParent() != null && getParent() instanceof Panel) {
			if (((Panel)getParent()).getTheme() != null) {
				return ((Panel)getParent()).getTheme();
			}		
		}
		return getMainWindow().getTheme();
	}
	private Design design;
	public Design getDesign() {
		if (design != null) {
			return design;
		}
		if (getParent() != null && getParent() instanceof Panel) {
			if (((Panel)getParent()).getDesign() != null) {
				return ((Panel)getParent()).getDesign();
			}		
		}
		return getMainWindow().getDesign();
	}
	private Taste taste;
	public Taste getTaste() {
		if (taste != null) {
			return taste;
		}
		if (getParent() != null && getParent() instanceof Panel) {
			if (((Panel)getParent()).getTaste() != null) {
				return ((Panel)getParent()).getTaste();
			}
		}
		return getMainWindow().getTaste();
	}
}
