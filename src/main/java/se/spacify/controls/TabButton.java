package se.spacify.controls;

import se.spacify.ui.MainWindow;
import se.spacify.ui.theme.ThemeManager;

import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A WMP-style tab: rounded top corners, sharp bottom, meant to sit flush on the
 * bottom edge of the navigation bar. The selected (active) tab is filled with
 * the content background so it reads as the foremost tab; unselected tabs are
 * flat and show a subtle highlight on hover.
 */
public class TabButton extends ToggleButton {

	private static final long serialVersionUID = 1L;
	public static final String ORIENTATION_HORIZONTAL = "horizontal";
	public static final String ORIENTATION_VERTICAL = "vertical";

	/**
	 * Explicit hover/press tracking. Under the Synth-based Nimbus L&F, the button
	 * model's rollover/pressed transitions don't reliably repaint a custom-painted,
	 * non-opaque button, so we track the state ourselves and force a repaint.
	 */
	private boolean hovered;
	private boolean pressed;
	private String orientation = ORIENTATION_HORIZONTAL;

	public TabButton(String text) {
		super(text);
		setContentAreaFilled(false);
		setBorderPainted(false);
		setFocusPainted(false);
		setOpaque(false);
		setRolloverEnabled(true);
		setBorder(BorderFactory.createEmptyBorder(10, 32, 8, 32));
		setFont(getFont().deriveFont(14f));
		setForeground(ThemeManager.getForeground());

		MouseAdapter mouse = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				hovered = true;
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				hovered = false;
				pressed = false;
				repaint();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					// Show the active (pressed) state immediately, but defer the
					// actual navigation until the button is released.
					pressed = true;
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				pressed = false;
				repaint();
			}
		};
		
		addMouseListener(mouse);

		ThemeManager.addChangeListener(() -> {
			if (isSelected()) {
				setForeground(Color.WHITE);
			} else {
				setForeground(ThemeManager.accentDark(0.2f));
			}
			repaint();
		});
	}

	public String getOrientation() {
		return orientation;
	}
	public void setOrientation(String value) {
		orientation = value;
	}
	
	/** True while the pointer is over the tab. */
	public boolean isHovered() {
		return hovered;
	}

	/** True while the tab is being held down (before release). */
	public boolean isPressedState() {
		return pressed;
	}

	@Override
	public Color getForeground() {
		// The Skin fills the tab with the active background while selected or
		// pressed, so keep the label white in both cases for legibility.
		if (pressed && !isSelected()) {
			return Color.WHITE;
		}
		return super.getForeground();
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		return new Dimension(d.width, 50);
	}

	@Override
	public void setSelected(boolean b) {
		super.setSelected(b);
		if (b) {
			setForeground(Color.WHITE);	
		} else {
			setForeground(ThemeManager.accentDark(0.2f));
		}
		//setFont(getFont().deriveFont(b ? Font.BOLD : Font.PLAIN));
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		

		((MainWindow)(SwingUtilities.getWindowAncestor(this))).getSkin().paintTabButton(this, g2);

		g2.dispose();

		super.paintComponent(g);   // draws text/icon (content area is disabled)
	}
}
