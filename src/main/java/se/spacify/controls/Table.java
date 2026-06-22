package se.spacify.controls;

import java.awt.Color;
import java.awt.Graphics;

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
	/** Row height in pixels, matching the Windows ListView default. */
	public static final int ROW_HEIGHT = 24;

	public Table() {
		setRowHeight(ROW_HEIGHT);
		this.getTableHeader().setDefaultRenderer(new TableHeaderRenderer(this));
		this.setDefaultRenderer(getClass(), new SpaceTableCellRenderer(this));
	}
	public Table(Object[][] data, String[] columns) {
		super(data, columns);
		setRowHeight(ROW_HEIGHT);
		this.getTableHeader().setDefaultRenderer(new TableHeaderRenderer(this));
		this.setDefaultRenderer(getClass(), new SpaceTableCellRenderer(this));
	}
	public Table(TableModel model) {
		super(model);
		setRowHeight(ROW_HEIGHT);
		this.getTableHeader().setDefaultRenderer(new TableHeaderRenderer(this));
		this.setDefaultRenderer(getClass(), new SpaceTableCellRenderer(this));
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

	/**
	 * Continue the zebra striping into the empty space below the last row, so a
	 * full-height table (fillsViewportHeight) shows stripes all the way to the
	 * bottom even when the data does not fill it — like a Windows ListView.
	 * Matches {@link se.spacify.ui.theme.ThemedTableCellRenderer}: even rows carry
	 * the alternate-background overlay, odd rows keep the base background.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int rowHeight = getRowHeight();
		if (rowHeight <= 0) return;
		int rowCount = getRowCount();
		int y = rowCount > 0 ? (int) getCellRect(rowCount - 1, 0, true).getMaxY() : 0;
		int height = getHeight();
		if (y >= height) return;   // table already full — nothing below the rows

		Color alt;
		try {
			alt = getSkin().getColorValue(this, "table.alternateBackground", new Color(0, 0, 0, 11));
		} catch (Exception e) {
			alt = new Color(0, 0, 0, 11);
		}

		int width = getWidth();
		// Virtual row indices continue the data rows' parity so the stripes line up.
		for (int row = rowCount; y < height; row++, y += rowHeight) {
			if (row % 2 == 0) {
				g.setColor(alt);
				g.fillRect(0, y, width, rowHeight);
			}
		}
	}
}
