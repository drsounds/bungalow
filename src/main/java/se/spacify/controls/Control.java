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
import se.spacify.dom.Element;
import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;
import se.spacify.ui.theme.Taste;
import se.spacify.ui.theme.Theme;

/**
 * Intermediate base for every Spacify control. Controls no longer inherit a Swing
 * widget directly; instead each one <em>wraps</em> one as its {@link #component}
 * (the {@code T} type parameter) and is reached through {@link #getComponent()}
 * (the "getComponent() bridge"). This gives the controls a single, maintainable
 * trait hierarchy — shared skin/theme/design/taste resolution and parent/child
 * wiring live here once — that is also straightforward to map onto a XUL XML
 * element tree (see {@link se.spacify.controls.XUL} and {@link Element}).
 *
 * <p>Resolution of {@link Skin}/{@link Theme}/{@link Design}/{@link Taste} walks
 * up the control tree (this → parent → … → {@link MainWindow}); set an explicit
 * value on a control to override it for that subtree.
 *
 * @param <T> the Swing component this control wraps
 */
public abstract class Control<T extends Component> {

	protected T component;
	protected Control<?> parent;
	protected final List<Control<?>> children = new ArrayList<>();

	/** Optional XUL/DOM element this control was built from (null when built in code). */
	protected Element element;

	private Theme theme;
	private Design design;
	private Skin skin;
	private Taste taste;

	private final Map<String, Object> attributes = new LinkedHashMap<>();

	protected Control() {
	}

	protected Control(Control<?> parent) {
		this.parent = parent;
	}

	// ── The Swing bridge ─────────────────────────────────────────────────────────

	/** The wrapped Swing component — the bridge into the Swing tree. */
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

	/** Add a child control, wiring parentage and mounting its component into ours. */
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

	// ── Common widget conveniences (delegate to the wrapped component) ───────────

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

	// ── Skin / Theme / Design / Taste resolution (formerly ControlTrait) ─────────

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

	// ── XUL / DOM hooks ──────────────────────────────────────────────────────────

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	/** Generic attribute bag used by the XUL mapping; subclasses may specialise. */
	public void setAttribute(String attrName, Object value) {
		attributes.put(attrName, value);
	}

	public Object getAttribute(String attrName, Object defaultValue) {
		return attributes.getOrDefault(attrName, defaultValue);
	}
}
