package se.spacify.controls;

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

/**
 * A menu item control wrapping a {@link JMenuItem}. Reach the widget through
 * {@link #getComponent()}.
 */
public class MenuItem extends Control<JMenuItem> {

	public MenuItem() {
		this.component = new JMenuItem();
	}

	public MenuItem(String text) {
		this.component = new JMenuItem(text);
	}

	public void addActionListener(ActionListener l) { component.addActionListener(l); }
	public void setText(String text) { component.setText(text); }
}
