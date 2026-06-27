package se.spacify.controls;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import se.spacify.design.Design;
import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;
import se.spacify.ui.theme.Taste;
import se.spacify.ui.theme.Theme;

/**
 * Intermediate base for every Spacify control. Controls do not inherit a Swing
 * widget; each one <em>wraps</em> one as its {@link #component} ({@code T}) and is
 * a clean abstraction over it. The only bridge into Swing's component model is
 * {@link #getComponent()} — and the {@link #add(Control) child-adding} methods,
 * which mount {@code child.getComponent()} into this control's component. All
 * widget configuration (layout, borders, geometry, text, …) is done through
 * {@code getComponent()} so the two models stay cleanly separated; only the
 * lifecycle conveniences {@link #repaint()}, {@link #revalidate()} and
 * {@link #setVisible(boolean)} are offered directly.
 *
 * <p>{@link Skin}/{@link Theme}/{@link Design}/{@link Taste} resolution walks up
 * the control tree (this → parent → … → {@link MainWindow}); set an explicit value
 * on a control to override it for that subtree.
 *
 * @param <T> the Swing component this control wraps
 */
public abstract class Control<T extends Component> {

	protected T component;
	protected Control<?> parent;
	protected final List<Control<?>> children = new ArrayList<>();

	private Theme theme;
	private Design design;
	private Skin skin;
	private Taste taste;
	private String name;

	private final Map<String, Object> attributes = new LinkedHashMap<>();

	protected Control() {
	}

	protected Control(Control<?> parent) {
		this.parent = parent;
	}

	// ── The Swing bridge ─────────────────────────────────────────────────────────

	/** The wrapped Swing component — the single bridge into the Swing tree. */
	public T getComponent() {
		return component;
	}

	protected void setComponent(T component) {
		this.component = component;
	}

	// ── Tree wiring ──────────────────────────────────────────────────────────────

	public Control<?> getParent() {
		return parent;
	}

	protected void setParent(Control<?> parent) {
		this.parent = parent;
	}

	public List<Control<?>> getChildren() {
		return children;
	}

	/**
	 * Add a child control: records it in the control tree and mounts its
	 * {@link #getComponent() component} into this control's component.
	 */
	public Control<T> add(Control<?> child) {
		children.add(child);
		child.setParent(this);
		if (component instanceof Container c && child.getComponent() != null) {
			c.add(child.getComponent());
		}
		return this;
	}

	/** Add a child control with a layout constraint (e.g. a {@code BorderLayout} side). */
	public Control<T> add(Control<?> child, Object constraints) {
		children.add(child);
		child.setParent(this);
		if (component instanceof Container c && child.getComponent() != null) {
			c.add(child.getComponent(), constraints);
		}
		return this;
	}

	public void remove(Control<?> child) {
		children.remove(child);
		if (component instanceof Container c && child.getComponent() != null) {
			c.remove(child.getComponent());
		}
	}

	// ── Lifecycle conveniences ───────────────────────────────────────────────────

	public void repaint() {
		if (component != null)
			component.repaint();
	}

	public void revalidate() {
		if (component instanceof JComponent j)
			j.revalidate();
	}

	public void setVisible(boolean visible) {
		if (component != null)
			component.setVisible(visible);
	}

	public boolean isVisible() {
		return component != null && component.isVisible();
	}

	// ── Skin / Theme / Design / Taste resolution ─────────────────────────────────

	public void setSkin(Skin skin) {
		this.skin = skin;
	}

	public Skin getSkin() {
		if (skin != null)
			return skin;
		if (parent != null && parent.getSkin() != null)
			return parent.getSkin();
		return getMainWindow().getSkin();
	}

	public void setTheme(Theme theme) {
		this.theme = theme;
	}

	public Theme getTheme() {
		if (theme != null)
			return theme;
		if (parent != null && parent.getTheme() != null)
			return parent.getTheme();
		return getMainWindow().getTheme();
	}

	public void setDesign(Design design) {
		this.design = design;
	}

	public Design getDesign() {
		if (design != null)
			return design;
		if (parent != null && parent.getDesign() != null)
			return parent.getDesign();
		return getMainWindow().getDesign();
	}

	public void setTaste(Taste taste) {
		this.taste = taste;
	}

	public Taste getTaste() {
		if (taste != null)
			return taste;
		if (parent != null && parent.getTaste() != null)
			return parent.getTaste();
		return getMainWindow().getTaste();
	}

	/**
	 * The owning window. While a control is still being constructed its component
	 * has no window ancestor yet, so we fall back to the live {@link MainWindow} so
	 * skin/theme/design/taste stay resolvable.
	 */
	public MainWindow getMainWindow() {
		java.awt.Window w = component != null ? SwingUtilities.getWindowAncestor(component) : null;
		if (w instanceof MainWindow mw)
			return mw;
		return MainWindow.getInstance();
	}

	// ── Control properties ───────────────────────────────────────────────────────

	/** Control name (used by {@link se.spacify.aspect.Aspect}); a Control concept, not the component's. */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/** Generic attribute bag used by the XUL mapping; subclasses may specialise. */
	public void setAttribute(String attrName, Object value) {
		attributes.put(attrName, value);
	}

	public Object getAttribute(String attrName, Object defaultValue) {
		return attributes.getOrDefault(attrName, defaultValue);
	}
}
