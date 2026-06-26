package se.spacify.controls;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * The base container control. Unlike the leaf widgets (which expose their wrapped
 * Swing component only through {@link #getComponent()}), {@code Panel} also offers
 * a thin <em>container facade</em> — {@link #add}, {@link #setLayout},
 * {@link #setBorder}, etc. delegate to the wrapped {@link JPanel} — so the view
 * layer ({@link se.spacify.navigation.View}, {@code ViewStack}, chrome, sidebars)
 * can be written against {@code Panel} naturally while still being HAS-A controls.
 *
 * <p>Custom painting is done by overriding {@link #paintSurface(Graphics)} rather
 * than a Swing {@code paintComponent}; the wrapped {@link Surface} routes painting
 * back here so subclasses never touch Swing directly.
 */
public class Panel extends Control<JPanel> {

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

	public Panel() {
		this.component = new Surface();
	}

	public Panel(LayoutManager layout) {
		this.component = new Surface(layout);
	}

	/**
	 * Paint this panel's surface. Default does the standard panel painting; override
	 * to draw a skinned background (call {@code super.paintSurface(g)} to keep it).
	 */
	protected void paintSurface(Graphics g) {
		((Surface) component).superPaint(g);
	}

	// ── Container facade (delegates to the wrapped JPanel) ───────────────────────

	public void setLayout(LayoutManager layout)        { component.setLayout(layout); }
	public LayoutManager getLayout()                   { return component.getLayout(); }

	public Component add(Component c)                   { return component.add(c); }
	public Component add(Component c, int index)        { return component.add(c, index); }
	public void add(Component c, Object constraints)    { component.add(c, constraints); }

	/** Add a child control by mounting its component (records it in the control tree). */
	@Override
	public Control<JPanel> add(Control<?> child) {
		return super.add(child);
	}

	@Override
	public Control<JPanel> add(Control<?> child, Object constraints) {
		return super.add(child, constraints);
	}

	public void remove(Component c)                    { component.remove(c); }
	public void removeAll()                            { component.removeAll(); }
	public Component[] getComponents()                 { return component.getComponents(); }

	public void setBorder(Border border)               { component.setBorder(border); }
	public Border getBorder()                          { return component.getBorder(); }
	public void setOpaque(boolean opaque)              { component.setOpaque(opaque); }
	public boolean isOpaque()                          { return component.isOpaque(); }

	public void setBackground(Color c)                 { component.setBackground(c); }
	public Color getBackground()                       { return component.getBackground(); }
	public void setForeground(Color c)                 { component.setForeground(c); }
	public Color getForeground()                       { return component.getForeground(); }

	public void setFont(Font f)                        { component.setFont(f); }
	public Font getFont()                              { return component.getFont(); }

	public int getWidth()                              { return component.getWidth(); }
	public int getHeight()                             { return component.getHeight(); }
	public Dimension getSize()                         { return component.getSize(); }

	public void setPreferredSize(Dimension d)          { component.setPreferredSize(d); }
	public Dimension getPreferredSize()                { return component.getPreferredSize(); }
	public void setMinimumSize(Dimension d)            { component.setMinimumSize(d); }
	public void setMaximumSize(Dimension d)            { component.setMaximumSize(d); }

	public void putClientProperty(Object key, Object value) { component.putClientProperty(key, value); }
	public Object getClientProperty(Object key)        { return component.getClientProperty(key); }

	/** The wrapped panel as a {@link JComponent} (alias of {@link #getComponent()}). */
	public JComponent getView() { return component; }
}
