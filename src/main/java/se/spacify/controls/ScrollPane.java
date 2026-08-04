package se.spacify.controls;

import javax.swing.JScrollPane;

/**
 * A scrollable container control. Independent of any UI toolkit: the active
 * {@link se.spacify.ui.render.UserInterface} creates this control's native
 * peer (a {@link JScrollPane} for Swing, see {@link #createSwingPeer()}).
 * Swing-only code that needs the concrete widget can use
 * {@link #getSwingComponent()}.
 */
public class ScrollPane extends Control<Object> {

	public ScrollPane() {
		initNative();
	}

	/** @param viewport the control whose native peer becomes the scrollable content. */
	public ScrollPane(Control<?> viewport) {
		initNative();
		if (viewport != null) add(viewport);
	}

	/** Builds this scroll pane's Swing peer. Called only by {@code SwingUserInterface}. */
	public JScrollPane createSwingPeer() {
		return new JScrollPane();
	}

	public JScrollPane getSwingComponent() {
		return getComponent() instanceof JScrollPane sp ? sp : null;
	}
}
