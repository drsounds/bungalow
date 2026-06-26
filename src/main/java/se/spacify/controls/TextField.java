package se.spacify.controls;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JTextField;

/**
 * A single-line text input control wrapping a {@link JTextField}. Reach the widget
 * through {@link #getComponent()}; the wrapped {@link Surface} paints the skinned
 * field background before the text.
 */
public class TextField extends Control<JTextField> {

	protected class Surface extends JTextField {
		private static final long serialVersionUID = 1L;
		Surface() { super(); }
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

	public TextField() {
		this.component = new Surface();
	}

	public TextField(String text) {
		this.component = new Surface(text);
	}

	// ── Common JTextField facade ─────────────────────────────────────────────────
	public void setText(String text)            { component.setText(text); }
	public String getText()                     { return component.getText(); }
	public void addActionListener(java.awt.event.ActionListener l) { component.addActionListener(l); }
	public void putClientProperty(Object k, Object v) { component.putClientProperty(k, v); }
	public void setPreferredSize(java.awt.Dimension d) { component.setPreferredSize(d); }
	public void setColumns(int n)               { component.setColumns(n); }
	public void setFont(java.awt.Font f)        { component.setFont(f); }
	public java.awt.Font getFont()              { return component.getFont(); }
	public void setEnabled(boolean b)           { component.setEnabled(b); }
}
