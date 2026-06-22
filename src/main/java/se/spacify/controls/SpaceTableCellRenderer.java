package se.spacify.controls;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LabelUI;
import javax.swing.table.TableCellRenderer;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;

public class SpaceTableCellRenderer extends Label implements TableCellRenderer {
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
	
	public SpaceTableCellRenderer(Table table) { 
        super();
        this.table = table;
    }

    private class SpaceTableCellLabelUI extends LabelUI {

		// Define your custom padding/insets
    	private static final Insets BUTTON_PADS = new Insets( 4, 4, 4, 4);
		@Override
		public Dimension getPreferredSize(JComponent c) {
			Label b = (Label)SpaceTableCellRenderer.this;

			// Get the font metrics to measure text width/height
			FontMetrics fm = b.getFontMetrics(b.getFont());
			
			Rectangle viewR = new Rectangle();
			Rectangle iconR = new Rectangle();
			Rectangle textR = new Rectangle();

			// This utility method calculates the necessary text and icon bounds
			SwingUtilities.layoutCompoundLabel(
				c, fm, b.getText(), b.getIcon(),
				b.getVerticalAlignment(), b.getHorizontalAlignment(),
				b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
				viewR, iconR, textR, 
				b.getText() == null ? 0 : b.getIconTextGap()
			);

			// Combine text and icon rectangles to find total content width/height
			Rectangle totalBounds = iconR.union(textR);

			// Add your custom padding + the button's standard borders/insets
			Insets insets = b.getInsets();
			int width = totalBounds.width + BUTTON_PADS.left + BUTTON_PADS.right + insets.left + insets.right;
			int height = totalBounds.height + BUTTON_PADS.top + BUTTON_PADS.bottom + insets.top + insets.bottom;

			return new Dimension(width, height);
		}
		@Override
		public void paint(Graphics g, JComponent c) {
			// TODO Auto-generated method stub
			JLabel l = (JLabel)c;
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
			// 2. Initialize layout rectangles
			
			FontMetrics fm = g2.getFontMetrics(l.getFont());
			Insets insets = l.getInsets();

			Rectangle viewR = new Rectangle(insets.left, insets.top, 
								c.getWidth() - (insets.left + insets.right), 
								c.getHeight() - (insets.top + insets.bottom));
			Rectangle iconR = new Rectangle();
			Rectangle textR = new Rectangle();

			// 3. Layout the text and icon positions relative to the current component bounds
			String text = SwingUtilities.layoutCompoundLabel(
				c, fm, l.getText(), l.getIcon(),
				l.getVerticalAlignment(),l.getHorizontalAlignment(),
				l.getVerticalTextPosition(),l.getHorizontalTextPosition(),
				viewR, iconR, textR, 
				l.getText() == null ? 0 : l.getIconTextGap()
			);

			// 4. Paint the Icon if it exists
			if (l.getIcon() != null) {
				l.getIcon().paintIcon(c, g2, iconR.x, iconR.y);
			}

			// 5. Paint the Text properly
			if (text != null && !text.isEmpty()) {
				// basicButtonUI's paintText handles alignment, mnemonics, and disabled states
				getSkin().paintText(l, g2, text, textR.x, textR.y + fm.getAscent());
			}
			g2.dispose();
			super.paint(g, c);
		}	
	}

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
    	
        setText(value.toString());
        setUI(new SpaceTableCellLabelUI());
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