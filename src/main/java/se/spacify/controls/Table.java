package se.spacify.controls;

import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import se.spacify.ui.theme.ThemeManager;
import se.spacify.ui.theme.ThemedTableCellRenderer;

/**
 * A table control. Independent of any UI toolkit at the type level: the
 * active {@link se.spacify.ui.render.UserInterface} creates this control's
 * native peer — currently only implemented for Swing (a {@link JTable} with
 * Spacify's row height and skinned header/cell renderers, see
 * {@link #createSwingPeer()}). Bridging {@link TableModel}-based data to a
 * fundamentally different Jexer table widget is a larger follow-up; the
 * Jexer backend simply doesn't support {@code Table} yet. Swing-only code
 * that needs the concrete widget can use {@link #getSwingComponent()}.
 */
public class Table extends Control<Object> {

	/** Row height in pixels, matching the Windows ListView default. */
	public static final int ROW_HEIGHT = 24;

	private Object[][] data;
	private String[] columns;
	private TableModel model;

	public Table() {
		initNative();
	}

	public Table(Object[][] data, String[] columns) {
		this.data = data;
		this.columns = columns;
		initNative();
	}

	public Table(TableModel model) {
		this.model = model;
		initNative();
	}

	// ── Swing peer (used only by se.spacify.ui.render.swing.SwingUserInterface) ───

	/**
	 * A {@link JTable} that, when {@link ThemeManager#isFillEmptyRows()} is on,
	 * keeps painting the zebra striping in the blank area below the last row so a
	 * short list still fills the viewport with stripes instead of flat background.
	 */
	private static final class ZebraTable extends JTable {
		private static final long serialVersionUID = 1L;

		ZebraTable() { super(); }
		ZebraTable(Object[][] data, String[] columns) { super(data, columns); }
		ZebraTable(TableModel model) { super(model); }

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (!ThemeManager.isFillEmptyRows())
				return;
			int rowH = getRowHeight();
			if (rowH <= 0)
				return;
			int rows = getRowCount();
			int top  = rows == 0 ? 0 : getCellRect(rows - 1, 0, true).y + rowH;
			int h = getHeight(), w = getWidth();
			for (int y = top, r = rows; y < h; y += rowH, r++) {
				g.setColor(ThemedTableCellRenderer.rowBackground(this, r));
				g.fillRect(0, y, w, rowH);
			}
		}
	}

	/** Builds this table's Swing peer. Called only by {@code SwingUserInterface}. */
	public JTable createSwingPeer() {
		ZebraTable t = model != null ? new ZebraTable(model)
			: columns != null ? new ZebraTable(data, columns)
			: new ZebraTable();
		t.setRowHeight(ROW_HEIGHT);
		t.getTableHeader().setDefaultRenderer(new TableHeaderRenderer(this));
		t.setDefaultRenderer(Table.class, new SpaceTableCellRenderer(this));
		return t;
	}

	public JTable getSwingComponent() {
		return getComponent() instanceof JTable t ? t : null;
	}
}
