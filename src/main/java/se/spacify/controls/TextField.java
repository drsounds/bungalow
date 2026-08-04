package se.spacify.controls;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JTextField;

/**
 * A single-line text input control. Independent of any UI toolkit: the active
 * {@link se.spacify.ui.render.UserInterface} creates and binds this control's
 * native peer (a {@link JTextField} for Swing, see {@link #createSwingPeer()}),
 * keeping {@link #getText()} in sync with whatever the user types. Swing-only
 * code that needs the concrete widget can use {@link #getSwingComponent()}.
 */
public class TextField extends Control<Object> {

	private String text = "";
	private final List<Consumer<String>> changeListeners = new ArrayList<>();

	public TextField() {
		initNative();
	}

	public TextField(String text) {
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

	/** Register a listener fired whenever the native peer's text changes (typing, paste, …). */
	public void addChangeListener(Consumer<String> listener) {
		changeListeners.add(listener);
	}

	/**
	 * Called by the active {@link se.spacify.ui.render.UserInterface} when the
	 * native peer's text changed directly (not via {@link #setText}) — updates
	 * the field without echoing back to the native peer.
	 */
	public void setTextFromNative(String text) {
		this.text = text;
		for (Consumer<String> l : changeListeners) l.accept(text);
	}

	// ── Swing peer (used only by se.spacify.ui.render.swing.SwingUserInterface) ───

	protected class Surface extends JTextField {
		private static final long serialVersionUID = 1L;
		Surface(String text) { super(text); }
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			getSkin().paintTextField(TextField.this, g2);
			g2.dispose();
			super.paintComponent(g);
		}
	}

	/** Builds this text field's Swing peer. Called only by {@code SwingUserInterface}. */
	public JTextField createSwingPeer() {
		return new Surface(text);
	}

	public JTextField getSwingComponent() {
		return getComponent() instanceof JTextField t ? t : null;
	}
}
