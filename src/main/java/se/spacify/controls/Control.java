package se.spacify.controls;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import se.spacify.design.Design;
import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;
import se.spacify.ui.render.Reconciler;
import se.spacify.ui.render.UserInterface;
import se.spacify.ui.render.UserInterfaceManager;
import se.spacify.ui.theme.Taste;
import se.spacify.ui.theme.Theme;

/**
 * Intermediate base for every Spacify control. A control is not tied to any UI
 * toolkit: {@code T} is whatever native peer the active
 * {@link se.spacify.ui.render.UserInterface} backend produces for it (a Swing
 * {@link Component} for the Swing backend, a Jexer widget for the Jexer
 * backend, …). Controls that predate this abstraction still build their own
 * (Swing) native peer directly in their constructor and keep working
 * unchanged; controls ported to be backend-neutral instead call
 * {@link #initNative()}, which asks the active {@link UserInterface} to create
 * and bind the peer. Either way, tree mutations ({@link #add(Control)},
 * {@link #remove(Control)}) are reported to the {@link Reconciler}, which is
 * the single place that knows how to mount/unmount a native peer for the
 * active backend.
 *
 * <p>{@link Skin}/{@link Theme}/{@link Design}/{@link Taste} resolution walks up
 * the control tree (this → parent → … → {@link MainWindow}); set an explicit value
 * on a control to override it for that subtree.
 *
 * @param <T> this control's native peer type, when one meaningful concrete type
 *            exists (unported, toolkit-specific controls); {@code Object} for
 *            controls that work across backends.
 */
public abstract class Control<T> {

	protected T component;
	protected Control<?> parent;
	protected final List<Control<?>> children = new ArrayList<>();

	private Theme theme;
	private Design design;
	private Skin skin;
	private Taste taste;
	private String name;

	public int getWidth() {
		return component instanceof Component c ? c.getWidth() : 0;
	}
	public int getHeight() {
		return component instanceof Component c ? c.getHeight() : 0;
	}
	public Color getBackground() {
		return component instanceof Component c ? c.getBackground() : null;
	}

	public Color getForeground() {
		return component instanceof Component c ? c.getForeground() : null;
	}

	private final Map<String, Object> attributes = new LinkedHashMap<>();

	protected Control() {
	}

	protected Control(Control<?> parent) {
		this.parent = parent;
	}

	// ── The native peer ──────────────────────────────────────────────────────────

	/** This control's native peer, or {@code null} if none has been created yet. */
	public T getComponent() {
		return component;
	}

	protected void setComponent(T component) {
		this.component = component;
	}

	/**
	 * For controls ported to be backend-neutral: ask the active
	 * {@link UserInterface} to create and {@link UserInterface#bind bind} this
	 * control's native peer. Called once, from the constructor, after any
	 * constructor-argument fields (e.g. initial text) have been assigned, so the
	 * backend can seed the peer's initial state from them.
	 *
	 * <p>Some backends (e.g. Jexer, whose widgets are constructed already
	 * attached to a parent) cannot create a peer before this control has a
	 * parent, and {@link UserInterface#createNative} legitimately returns
	 * {@code null} here; the {@link Reconciler} retries once this control is
	 * added to a mounted parent, via {@link #adoptNative}.
	 */
	@SuppressWarnings("unchecked")
	protected final void initNative() {
		UserInterface ui = activeUserInterface();
		if (ui == null) return;
		Object native_ = ui.createNative(this);
		if (native_ != null) {
			this.component = (T) native_;
			ui.bind(this, native_);
		}
	}

	/**
	 * The backend to delegate to: the live app's active backend, or — for a
	 * control built with no {@link MainWindow} running (e.g. a unit test) —
	 * {@link UserInterfaceManager#standalone()}, so controls stay constructible
	 * in isolation.
	 */
	private UserInterface activeUserInterface() {
		MainWindow mw = MainWindow.getInstance();
		return mw != null ? mw.getUserInterfaceManager().getActive() : UserInterfaceManager.standalone();
	}

	/**
	 * Set by a {@link UserInterface}/{@link Reconciler} once this control's
	 * native peer is created — including "created late", after this control
	 * already has a parent. Not for general use.
	 */
	@SuppressWarnings("unchecked")
	public final void adoptNative(Object native_) {
		this.component = (T) native_;
	}

	/**
	 * For controls ported to be backend-neutral: push a single property change to
	 * the active backend's {@link UserInterface#applyProperty}, if this control's
	 * native peer has been created.
	 */
	protected final void applyProperty(String key, Object value) {
		UserInterface ui = activeUserInterface();
		if (ui != null && component != null) {
			ui.applyProperty(this, key, value);
		}
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
	 * Add a child control: records it in the control tree and, via the
	 * {@link Reconciler}, mounts its native peer into this control's.
	 */
	public Control<T> add(Control<?> child) {
		children.add(child);
		child.setParent(this);
		Reconciler.get().onChildAdded(this, child, children.size() - 1, null);
		return this;
	}

	/** Add a child control with a layout constraint (e.g. a {@code BorderLayout} side). */
	public Control<T> add(Control<?> child, Object constraints) {
		children.add(child);
		child.setParent(this);
		Reconciler.get().onChildAdded(this, child, children.size() - 1, constraints);
		return this;
	}

	public void remove(Control<?> child) {
		children.remove(child);
		Reconciler.get().onChildRemoved(this, child);
	}

	// ── Lifecycle conveniences ───────────────────────────────────────────────────

	public void paint(Graphics g2) {

	}

	public void repaint() {
		if (component instanceof Component c) c.repaint();
	}

	public void revalidate() {
		if (component instanceof JComponent j) j.revalidate();
	}

	public void setVisible(boolean visible) {
		if (component instanceof Component c) c.setVisible(visible);
	}

	public boolean isVisible() {
		return component instanceof Component c && c.isVisible();
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

	/*
	 * XUL like View parser with following element schema:
	 * <button> = button
	 * <element> = Control
	 * <hbox> = HBox : Control<HBox>
	 * <vbox> = VBox : Control<VBox>
	 * <view> = ui.navigation.TabBarView - TabbedPane : Control<TabbedPane> with children <page> TabbedPane pages, with title = label attribute, that can host other components like a panel
	 * etc.
	 */

	/** Constant under which a control records the XUL tag it was rendered from, used to reconcile by type. */
	private static final String XUL_TAG = "xul:tag";

	/**
	 * The child controls created by the last {@link #setInnerXul} render, in document
	 * order. Tracked separately from {@link #children} so reconciliation can match a
	 * new element tree against the controls already on screen and keep their state.
	 */
	private final List<Control<?>> xulChildren = new ArrayList<>();

	/**
	 * Render {@code root}'s child elements as this control's children, much like
	 * setting {@code innerHTML}. Each tag maps to a control (see the schema above);
	 * the render <em>reconciles</em> against the previous one the way React does: a
	 * child whose tag matches the control already at that position is reused (and its
	 * attributes/text/subtree updated in place) so live state — selection, scroll,
	 * focus, caret — survives; only a differing tag forces a replacement.
	 */
	public void setInnerXul(Element root) {
		reconcileChildren(elementChildren(root));
		revalidate();
		repaint();
	}

	/** Syntactic sugar for {@link #setInnerXul(Element)} that parses {@code text} to a root element first. */
	public void setInnerXul(String text) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(text)));
			setInnerXul(doc.getDocumentElement());
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to parse XUL: " + text, e);
		}
	}

	/** Reconcile this control's XUL-managed children against a new list of element children. */
	private void reconcileChildren(List<Element> elements) {
		for (int i = 0; i < elements.size(); i++) {
			Element element = elements.get(i);
			String tag = element.getTagName();
			Control<?> existing = i < xulChildren.size() ? xulChildren.get(i) : null;

			// Reuse the control already at this slot when its tag matches; otherwise
			// create a fresh one and splice it into the tree at the same position.
			if (existing == null || !tag.equals(existing.getAttribute(XUL_TAG, null))) {
				Control<?> created = createControl(tag);
				created.setAttribute(XUL_TAG, tag);
				if (existing != null) {
					replaceChild(existing, created);
					xulChildren.set(i, created);
				} else {
					mountXulChild(created, element);
					xulChildren.add(created);
				}
				existing = created;
			}
			applyElement(existing, element);
		}

		// Drop trailing controls the new tree no longer has.
		while (xulChildren.size() > elements.size()) {
			remove(xulChildren.remove(xulChildren.size() - 1));
		}
	}

	/** Push attributes, direct text and the child subtree from {@code element} onto {@code control}. */
	private static void applyElement(Control<?> control, Element element) {
		NamedNodeMap attrs = element.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Node attr = attrs.item(i);
			String name = attr.getNodeName();
			String value = attr.getNodeValue();
			control.setAttribute(name, value);
			switch (name) {
				case "name"   -> control.setName(value);
				case "hidden" -> control.setVisible("false".equalsIgnoreCase(value));
				default       -> { /* plain attribute, kept in the bag */ }
			}
		}

		String text = directText(element);
		if (text != null) {
			setControlText(control, text);
		}

		// Recurse: the element's own children become the control's children.
		control.setInnerXul(element);
	}

	/**
	 * Map a tag name to its control. A tag containing a {@code '.'} is treated as a
	 * fully-qualified class name and instantiated by reflection (Android-style, e.g.
	 * {@code <se.spacify.controls.GlossyButton>}); short names use the built-in
	 * vocabulary below, with {@code <element>} and any unknown short tag becoming a
	 * plain {@link Panel}.
	 */
	private static Control<?> createControl(String tag) {
		if (tag.indexOf('.') >= 0) {
			return instantiate(tag);
		}
		return switch (tag) {
			case "button"               -> new Button();
			case "hbox"                 -> new HBox();
			case "vbox"                 -> new VBox();
			case "view"                 -> new TabbedPane();
			case "text", "label", "img" -> new Label();
			case "input"                -> new TextField();
			default                     -> new Panel();
		};
	}

	/**
	 * Reflectively build the control for a fully-qualified tag. The class must have a
	 * public no-arg constructor and be either a {@link Control} (used directly) or a
	 * Swing/AWT {@link Component} (wrapped in a {@link ComponentControl}).
	 */
	private static Control<?> instantiate(String className) {
		try {
			Object instance = loadClass(className).getDeclaredConstructor().newInstance();
			if (instance instanceof Control<?> control) {
				return control;
			}
			if (instance instanceof Component component) {
				return new ComponentControl(component);
			}
			throw new IllegalArgumentException(
				"XUL element <" + className + "> is neither a Control nor a Component");
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException("Cannot instantiate XUL element <" + className + ">", e);
		}
	}

	/**
	 * Resolve a fully-qualified XUL element class. The thread context classloader is
	 * tried first so a {@link Control} (or component) supplied by a separately-loaded
	 * plugin jar resolves; this class's own loader is the fallback for the built-ins.
	 */
	private static Class<?> loadClass(String className) throws ClassNotFoundException {
		ClassLoader context = Thread.currentThread().getContextClassLoader();
		if (context != null) {
			try {
				return Class.forName(className, true, context);
			} catch (ClassNotFoundException notInContext) {
				// Fall back to the loader that defined the control classes.
			}
		}
		return Class.forName(className, true, Control.class.getClassLoader());
	}

	/** Push text into the controls that carry text; a no-op for the rest. */
	private static void setControlText(Control<?> control, String text) {
		if (control instanceof Label l)          l.setText(text);
		else if (control instanceof TextField t) t.setText(text);
		else if (control instanceof Button b)    b.setText(text);
	}

	/** Mount a freshly created XUL child; a {@link TabbedPane} hosts it as a titled tab. */
	private void mountXulChild(Control<?> child, Element element) {
		if (this instanceof TabbedPane tabs) {
			String title = element.getAttribute("title");
			if (title.isEmpty()) {
				title = element.getAttribute("label");
			}
			tabs.addTab(title, child);
		} else {
			add(child);
		}
	}

	/** Replace {@code oldChild} with {@code newChild} in both the control tree and the native tree, in place. */
	private void replaceChild(Control<?> oldChild, Control<?> newChild) {
		int controlIndex = children.indexOf(oldChild);
		newChild.setParent(this);
		if (controlIndex >= 0) children.set(controlIndex, newChild);
		else                   children.add(newChild);
		Reconciler.get().onChildReplaced(this, oldChild, newChild, children.indexOf(newChild));
	}

	/** The direct element children of {@code element}, skipping text and comment nodes. */
	private static List<Element> elementChildren(Element element) {
		List<Element> out = new ArrayList<>();
		NodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				out.add((Element) node);
			}
		}
		return out;
	}

	/** The concatenated direct text of {@code element} (ignoring child elements), or {@code null} if blank. */
	private static String directText(Element element) {
		StringBuilder sb = new StringBuilder();
		NodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.TEXT_NODE) {
				String value = node.getNodeValue();
				if (value != null && !value.isBlank()) {
					sb.append(value.trim());
				}
			}
		}
		return sb.length() == 0 ? null : sb.toString();
	}

	/**
	 * The owning window. Spacify is single-window, so this is always the
	 * {@link MainWindow} singleton — resolvable even while a control is still
	 * being constructed and not yet attached to anything.
	 */
	public MainWindow getMainWindow() {
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
