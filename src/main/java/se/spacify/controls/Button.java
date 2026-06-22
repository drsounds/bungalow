package se.spacify.controls;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ButtonUI;

import java.awt.Insets;
import java.awt.Rectangle;

import se.spacify.design.Design;
import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;
import se.spacify.ui.theme.Taste;
import se.spacify.ui.theme.Theme;
import se.spacify.ui.theme.ThemeManager;

public class Button extends JButton implements Control {
	private boolean primary = false;
	public boolean getPrimary() {
		return primary;
	}
	public void setPrimary(boolean value) {
		primary = value;
	}
	private class SpaceButtonUI extends ButtonUI {
		
		// Define your custom padding/insets
    	private static final Insets BUTTON_PADS = new Insets(8, 28, 8, 28);

		@Override
		public Dimension getPreferredSize(JComponent c) {
			AbstractButton b = (AbstractButton) c;
			
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
			AbstractButton b = (AbstractButton)c;
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
			getSkin().paintButton((Button)c, g2, model.isRollover(), model.isPressed());
			// 2. Initialize layout rectangles
			
			FontMetrics fm = g2.getFontMetrics(b.getFont());
			Insets insets = c.getInsets();

			Rectangle viewR = new Rectangle(insets.left, insets.top, 
								c.getWidth() - (insets.left + insets.right), 
								c.getHeight() - (insets.top + insets.bottom));
			Rectangle iconR = new Rectangle();
			Rectangle textR = new Rectangle();

			// 3. Layout the text and icon positions relative to the current component bounds
			String text = SwingUtilities.layoutCompoundLabel(
				c, fm, b.getText(), b.getIcon(),
				b.getVerticalAlignment(), b.getHorizontalAlignment(),
				b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
				viewR, iconR, textR, 
				b.getText() == null ? 0 : b.getIconTextGap()
			);

			// 4. Paint the Icon if it exists
			if (b.getIcon() != null) {
				b.getIcon().paintIcon(c, g2, iconR.x, iconR.y);
			}

			// 5. Paint the Text properly
			if (text != null && !text.isEmpty()) {
				// basicButtonUI's paintText handles alignment, mnemonics, and disabled states
				getSkin().paintText(b, g2, text, textR.x, textR.y + fm.getAscent());
			}
			g2.dispose();
			super.paint(g, c);
		}
		
	}
	public Button() {	
        java.awt.event.MouseAdapter mouse = new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { repaint(); }
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) { repaint(); }
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { repaint(); }
        };

        ThemeManager.addChangeListener(this::repaint);
        addMouseListener(mouse);
		setUI(new SpaceButtonUI());
	}
	public Button(String text) {
		super(text);	
        java.awt.event.MouseAdapter mouse = new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { repaint(); }
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) { repaint(); }
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { repaint(); }
        };
        addMouseListener(mouse);
		setUI(new SpaceButtonUI());
	}

	public Button(Icon icon) {
		super(icon);	
        java.awt.event.MouseAdapter mouse = new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { repaint(); }
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) { repaint(); }
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { repaint(); }
        };
        addMouseListener(mouse);
		setUI(new SpaceButtonUI());
	}
 
	
	public Button(Icon icon, String text) {
		super(icon);
		setText(text);	
        java.awt.event.MouseAdapter mouse = new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { repaint(); }
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) { repaint(); }
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { repaint(); }
        };
        addMouseListener(mouse);
		setUI(new SpaceButtonUI());
	}
	public Skin getSkin() {
		return getMainWindow().getSkin();
	}
	public MainWindow getMainWindow() {
		return ((MainWindow)(SwingUtilities.getWindowAncestor(this)));
	}
	private static final long serialVersionUID = 1L;
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
