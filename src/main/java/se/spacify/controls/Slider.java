package se.spacify.controls;

import javax.swing.JSlider;

/**
 * A slider control wrapping a {@link JSlider}. Reach the widget through
 * {@link #getComponent()}.
 */
public class Slider extends Control<JSlider> {

	public Slider() {
		this.component = new JSlider();
	}

	public Slider(int orientation, int min, int max) {
		this.component = new JSlider(orientation, min, max);
	}
}
