package se.spacify.controls;

import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * A table control wrapping a {@link JTable} with Spacify's row height and the
 * skinned header/cell renderers installed. Reach the widget through
 * {@link #getComponent()}.
 */
public class Table extends Control<JTable> {

	/** Row height in pixels, matching the Windows ListView default. */
	public static final int ROW_HEIGHT = 24;

	public Table() {
		this.component = new JTable();
		init();
	}

	public Table(Object[][] data, String[] columns) {
		this.component = new JTable(data, columns);
		init();
	}

	public Table(TableModel model) {
		this.component = new JTable(model);
		init();
	}

	private void init() {
		component.setRowHeight(ROW_HEIGHT);
		component.getTableHeader().setDefaultRenderer(new TableHeaderRenderer(this));
		component.setDefaultRenderer(Table.class, new SpaceTableCellRenderer(this));
	}
}
