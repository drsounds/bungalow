package se.spacify.controls;

import java.awt.Graphics;
import java.awt.LayoutManager;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * The base container control. Independent of any UI toolkit: the active
 * {@link se.spacify.ui.render.UserInterface} creates this control's native
 * peer (a {@link JPanel} for Swing, see {@link #createSwingPeer()}). Swing-only
 * code that needs the concrete widget (layout tweaks, borders, …) can use
 * {@link #getSwingComponent()}.
 *
 * <p>Custom painting is done by overriding {@link #paintSurface(Graphics)}
 * rather than a Swing {@code paintComponent}; the wrapped {@link Surface}
 * (Swing backend only) routes painting back here so subclasses never need to
 * touch Swing directly.
 */
public class Panel extends Control<Object> {

	/** How {@link HBox}/{@link VBox} want their Swing peer's {@link BoxLayout} oriented. */
	public enum Axis { NONE, HORIZONTAL, VERTICAL }

	private Axis axis = Axis.NONE;
	private LayoutManager swingLayout;

	public Panel() {
		initNative();
	}

	/** @param layout a Swing layout manager; ignored by backends other than Swing. */
	public Panel(LayoutManager layout) {
		this.swingLayout = layout;
		initNative();
	}

	/** For {@link HBox}/{@link VBox}: request a box layout along {@code axis} once the native peer is created. */
	protected Panel(Axis axis) {
		this.axis = axis;
		initNative();
	}

	public Axis getAxis() {
		return axis;
	}

	// ── Swing peer (used only by se.spacify.ui.render.swing.SwingUserInterface) ───

	/** The wrapped JPanel; routes painting back to {@link Panel#paintSurface}. */
	protected class Surface extends JPanel {
		private static final long serialVersionUID = 1L;
		Surface() { super(); }
		Surface(LayoutManager layout) { super(layout); }
		@Override
		protected void paintComponent(Graphics g) {
			Panel.this.paintSurface(g);
		}
		/** The standard JPanel painting, for {@link Panel#paintSurface} to call. */
		void superPaint(Graphics g) {
			super.paintComponent(g);
		}
	}

	/** Builds this panel's Swing peer. Called only by {@code SwingUserInterface}. */
	public JPanel createSwingPeer() {
		Surface s = swingLayout != null ? new Surface(swingLayout) : new Surface();
		if (axis == Axis.HORIZONTAL) s.setLayout(new BoxLayout(s, BoxLayout.LINE_AXIS));
		else if (axis == Axis.VERTICAL) s.setLayout(new BoxLayout(s, BoxLayout.PAGE_AXIS));
		return s;
	}

	public JPanel getSwingComponent() {
		return getComponent() instanceof JPanel p ? p : null;
	}

	/**
	 * Paint this panel's surface. Default does the standard panel painting; override
	 * to draw a skinned background (call {@code super.paintSurface(g)} to keep it).
	 */
	protected void paintSurface(Graphics g) {
		if (getComponent() instanceof Surface s) s.superPaint(g);
	}
}
