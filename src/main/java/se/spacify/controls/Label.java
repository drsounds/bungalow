package se.spacify.controls;

import java.awt.Graphics;

import javax.swing.JLabel;

/**
 * A text/icon label control. Independent of any UI toolkit: the active
 * {@link se.spacify.ui.render.UserInterface} creates this control's native
 * peer (a {@link JLabel} for Swing, see {@link #createSwingPeer()}). Custom
 * painting is done by overriding {@link #paintSurface(Graphics)} (the wrapped
 * {@link Surface}, Swing backend only, routes here).
 */
public class Label extends Control<Object> {

	private String text;

	public Label() {
		initNative();
	}

	public Label(String text) {
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

	// ── Swing peer (used only by se.spacify.ui.render.swing.SwingUserInterface) ───

	/** The wrapped JLabel; routes painting back to {@link Label#paintSurface}. */
	protected class Surface extends JLabel {
		private static final long serialVersionUID = 1L;
		Surface() { super(); }
		Surface(String text) { super(text); }
		@Override
		protected void paintComponent(Graphics g) {
			Label.this.paintSurface(g);
		}
		void superPaint(Graphics g) {
			super.paintComponent(g);
		}
	}

	/** Builds this label's Swing peer. Called only by {@code SwingUserInterface}. */
	public JLabel createSwingPeer() {
		return text != null ? new Surface(text) : new Surface();
	}

	public JLabel getSwingComponent() {
		return getComponent() instanceof JLabel l ? l : null;
	}

	/** Override to custom-paint; default does the standard label painting. */
	protected void paintSurface(Graphics g) {
		if (getComponent() instanceof Surface s) s.superPaint(g);
	}
}
