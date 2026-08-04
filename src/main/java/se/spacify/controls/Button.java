package se.spacify.controls;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * The standard push-button control. Independent of any UI toolkit: the active
 * {@link se.spacify.ui.render.UserInterface} creates and binds this control's
 * native peer (a {@link JButton} for Swing, see {@link #createSwingPeer()}).
 * Swing-only code that needs the concrete widget can use
 * {@link #getSwingComponent()}.
 */
public class Button extends Control<Object> {

	private boolean primary = false;
	private String variant = "default";

	private String text;
	private Icon icon;
	private boolean enabled = true;
	private final List<Runnable> clickListeners = new ArrayList<>();

	public String getVariant() {
		return variant;
	}
	public void setVariant(String variant) {
		this.variant = variant;
	}
	public boolean getPrimary() { return primary; }
	public void setPrimary(boolean value) { primary = value; }

	public Button() {
		initNative();
	}

	public Button(String text) {
		this.text = text;
		initNative();
	}

	public Button(Icon icon) {
		this.icon = icon;
		initNative();
	}

	public Button(Icon icon, String text) {
		this.icon = icon;
		this.text = text;
		initNative();
	}

	// ── Backend-neutral property API ─────────────────────────────────────────────

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		applyProperty("text", text);
	}

	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
		applyProperty("icon", icon);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		applyProperty("enabled", enabled);
	}

	/** Register a click handler; fired from {@link #fireClicked()} regardless of backend. */
	public void addActionListener(Runnable listener) {
		clickListeners.add(listener);
	}

	/** Called by the active {@link se.spacify.ui.render.UserInterface} when the native peer is clicked. */
	public void fireClicked() {
		for (Runnable r : clickListeners) r.run();
	}

	// ── Swing peer (used only by se.spacify.ui.render.swing.SwingUserInterface) ───

	/** The wrapped JButton; routes painting and metrics back to the control. */
	protected class Surface extends JButton {
		private static final long serialVersionUID = 1L;
		Surface() { super(); }
		@Override
		protected void paintComponent(Graphics g) {
			Button.this.paintSurface(g);
		}
		@Override
		public Dimension getPreferredSize() {
			return Button.this.preferredSize(super.getPreferredSize());
		}
		void superPaint(Graphics g) { super.paintComponent(g); }
	}

	/** Paints the button face through the active skin (same logic as before). */
	private class SpaceButtonUI extends BasicButtonUI {

		private final Insets BUTTON_PADS = new Insets(8, 28, 8, 28);

		@Override
		public Dimension getPreferredSize(JComponent c) {
			AbstractButton b = (AbstractButton) c;
			FontMetrics fm = b.getFontMetrics(b.getFont());
			Rectangle viewR = new Rectangle();
			Rectangle iconR = new Rectangle();
			Rectangle textR = new Rectangle();
			SwingUtilities.layoutCompoundLabel(c, fm, b.getText(), b.getIcon(),
				b.getVerticalAlignment(), b.getHorizontalAlignment(),
				b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
				viewR, iconR, textR, b.getText() == null ? 0 : b.getIconTextGap());
			Rectangle totalBounds = iconR.union(textR);
			Insets insets = b.getInsets();
			int width = totalBounds.width + BUTTON_PADS.left + BUTTON_PADS.right + insets.left + insets.right;
			int height = totalBounds.height + BUTTON_PADS.top + BUTTON_PADS.bottom + insets.top + insets.bottom;
			return new Dimension(width, height);
		}

		@Override
		public void paint(Graphics g, JComponent c) {
			AbstractButton b = (AbstractButton) c;
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			getSkin().paintButton(Button.this, g2, b.getModel().isRollover(), b.getModel().isPressed());

			FontMetrics fm = g2.getFontMetrics(b.getFont());
			Insets insets = c.getInsets();
			Rectangle viewR = new Rectangle(insets.left, insets.top,
								c.getWidth() - (insets.left + insets.right),
								c.getHeight() - (insets.top + insets.bottom));
			Rectangle iconR = new Rectangle();
			Rectangle textR = new Rectangle();
			String text = SwingUtilities.layoutCompoundLabel(c, fm, b.getText(), b.getIcon(),
				b.getVerticalAlignment(), b.getHorizontalAlignment(),
				b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
				viewR, iconR, textR, b.getText() == null ? 0 : b.getIconTextGap());
			if (b.getIcon() != null) {
				b.getIcon().paintIcon(c, g2, iconR.x, iconR.y);
			}
			if (text != null && !text.isEmpty()) {
				getSkin().paintText(b, g2, text, textR.x, textR.y + fm.getAscent());
			}
			g2.dispose();
		}
	}

	/** Builds and fully configures this button's Swing peer. Called only by {@code SwingUserInterface}. */
	public JButton createSwingPeer() {
		Surface s = new Surface();
		if (text != null) s.setText(text);
		if (icon != null) s.setIcon(icon);
		s.setEnabled(enabled);
		s.getModel().addChangeListener(e -> repaint());
		java.awt.event.MouseAdapter mouse = new java.awt.event.MouseAdapter() {
			@Override public void mouseEntered(java.awt.event.MouseEvent e) { repaint(); }
			@Override public void mouseExited(java.awt.event.MouseEvent e)  { repaint(); }
			@Override public void mousePressed(java.awt.event.MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) { repaint(); }
			}
			@Override public void mouseReleased(java.awt.event.MouseEvent e) { repaint(); }
		};
		s.setOpaque(false);
		s.addMouseListener(mouse);
		s.setRolloverEnabled(true);
		s.setUI(new SpaceButtonUI());
		return s;
	}

	public JButton getSwingComponent() {
		return getComponent() instanceof JButton b ? b : null;
	}

	/** Override to extend painting; default runs the installed {@link SpaceButtonUI}. */
	protected void paintSurface(Graphics g) {
		if (getComponent() instanceof Surface s) s.superPaint(g);
	}

	/** Override to adjust the preferred size (default delegates to the UI). */
	protected Dimension preferredSize(Dimension dflt) {
		return dflt;
	}
}
