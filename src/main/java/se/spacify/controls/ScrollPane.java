package se.spacify.controls;

import javax.swing.JScrollPane;

public class ScrollPane extends Control<JScrollPane> {
	public ScrollPane() {
		super();
		this.component = new JScrollPane();
	}
	public ScrollPane(Control<?> parent) {
		super(parent);
		this.component = new JScrollPane(parent.getComponent());
	}
}
