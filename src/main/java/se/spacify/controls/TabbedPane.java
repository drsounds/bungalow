package se.spacify.controls;

import javax.swing.JTabbedPane;

import se.spacify.ui.render.Reconciler;

/**
 * A tabbed container control. Independent of any UI toolkit: the active
 * {@link se.spacify.ui.render.UserInterface} creates this control's native
 * peer (a {@link JTabbedPane} for Swing, see {@link #createSwingPeer()});
 * child controls are added (with a tab title) via {@link #addTab(String, Control)}.
 * Swing-only code that needs the concrete widget can use
 * {@link #getSwingComponent()}.
 */
public class TabbedPane extends Control<Object> {

	public TabbedPane() {
		initNative();
	}

	/** Add a child control as a titled tab. */
	public Control<Object> addTab(String title, Control<?> child) {
		children.add(child);
		child.setParent(this);
		Reconciler.get().onChildAdded(this, child, children.size() - 1, title);
		return this;
	}

	/** Builds this tabbed pane's Swing peer. Called only by {@code SwingUserInterface}. */
	public JTabbedPane createSwingPeer() {
		return new JTabbedPane();
	}

	public JTabbedPane getSwingComponent() {
		return getComponent() instanceof JTabbedPane tp ? tp : null;
	}
}
