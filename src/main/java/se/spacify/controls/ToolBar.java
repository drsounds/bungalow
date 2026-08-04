package se.spacify.controls;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JToolBar;

import se.spacify.ui.theme.ThemeManager;

/**
 * A toolbar container control. Independent of any UI toolkit: the active
 * {@link se.spacify.ui.render.UserInterface} creates this control's native
 * peer (a {@link JToolBar} for Swing, see {@link #createSwingPeer()}, painted
 * by the active skin via {@link se.spacify.skinning.Skin#paintToolBar}; a
 * plain Jexer container for the Jexer backend). Swing-only code that needs
 * the concrete widget can use {@link #getSwingComponent()}.
 */
public class ToolBar extends Control<Object> {

	public ToolBar() {
		initNative();
	}

	// ── Swing peer (used only by se.spacify.ui.render.swing.SwingUserInterface) ───

	protected class Surface extends JToolBar {
		private static final long serialVersionUID = 1L;
		Surface() { super(); }
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			getSkin().paintToolBar(ToolBar.this, g2);
			g2.dispose();
		}
	}

	/** Builds this toolbar's Swing peer. Called only by {@code SwingUserInterface}. */
	public JToolBar createSwingPeer() {
		Surface s = new Surface();
		s.setFloatable(false);
		s.setOpaque(true);
		s.setBackground(ThemeManager.getTintColor());
		return s;
	}

	public JToolBar getSwingComponent() {
		return getComponent() instanceof JToolBar t ? t : null;
	}
}
