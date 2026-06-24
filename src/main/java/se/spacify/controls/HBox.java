package se.spacify.controls;

import javax.swing.BoxLayout;

/**
 * A horizontal box container: a {@link Panel} that lays its children left-to-right.
 */
public class HBox extends Panel {

	public HBox() {
		super();
		setLayout(new BoxLayout(getComponent(), BoxLayout.LINE_AXIS));
	}
}
