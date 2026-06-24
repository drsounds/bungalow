package se.spacify.controls;

import java.awt.Component;

import javax.swing.JTabbedPane;

/**
 * A tabbed container control wrapping a {@link JTabbedPane}. Reach the widget
 * through {@link #getComponent()}; a small add facade is offered for convenience.
 */
public class TabbedPane extends Control<JTabbedPane> {

	public TabbedPane() {
		this.component = new JTabbedPane();
	}

	public void addTab(String title, Component c) { component.addTab(title, c); }

	public Control<JTabbedPane> addTab(String title, Control<?> child) {
		children.add(child);
		child.setParent(this);
		if (child.getComponent() != null) component.addTab(title, child.getComponent());
		return this;
	}
}
