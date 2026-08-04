package se.spacify.ui.render.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import se.spacify.controls.Button;
import se.spacify.controls.Control;
import se.spacify.controls.Label;
import se.spacify.controls.Panel;
import se.spacify.controls.ScrollPane;
import se.spacify.controls.SplitPane;
import se.spacify.controls.TabbedPane;
import se.spacify.controls.Table;
import se.spacify.controls.TextField;
import se.spacify.controls.ToolBar;
import se.spacify.controls.Tree;
import se.spacify.ui.WindowResizer;
import se.spacify.ui.render.UserInterface;

/**
 * The default backend: renders the {@link Control} tree with {@code javax.swing}.
 * Byte-for-byte the same visual result as before this abstraction existed.
 *
 * <p>Most of {@code se.spacify.controls} has not been ported to the
 * {@link UserInterface} abstraction yet — those classes still construct their
 * own Swing widget directly in their constructor, and mount it via
 * {@code Control.add()} as their tree is built, so by the time this backend
 * is asked to attach a root control, its whole subtree is already correctly
 * mounted; {@link #createNative} is simply never called for them.
 */
public class SwingUserInterface extends UserInterface {

	public static final String ID = "swing";

	private JFrame frame;
	private final Map<Control<?>, Runnable> unbindHooks = new IdentityHashMap<>();

	public SwingUserInterface() {
		super(ID, "Swing");
	}

	// ── Lifecycle ─────────────────────────────────────────────────────────────────

	@Override
	public void start(Control<?> root) {
		// No Reconciler.mountRoot() call here: Swing controls create their native
		// peer eagerly and Control.add() mounts each child into its parent
		// immediately as the tree is built, so by the time a root reaches here its
		// whole subtree is already fully (and correctly) mounted — re-walking and
		// re-mounting it would double-add already-embedded children, which is
		// fatal for containers with fixed slots (e.g. JSplitPane's left/right).
		// Only the root's own native needs attaching, to the real top-level window.

		frame = new JFrame("Spacify");
		frame.setUndecorated(true);  // remove native title bar + border on all platforms
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1100, 700);
		frame.setMinimumSize(new Dimension(800, 500));
		frame.setLocationRelativeTo(null);
		// 1px border so the window edge is visible against the desktop
		frame.getRootPane().setBorder(BorderFactory.createLineBorder(new Color(40, 40, 40), 1));

		Object rootNative = nativeFor(root);
		if (rootNative instanceof Component c) {
			frame.add(c, BorderLayout.CENTER);
		}

		WindowResizer.install(frame);
		frame.setVisible(true);
	}

	@Override
	public void stop() {
		if (frame != null) {
			frame.dispose();
			frame = null;
		}
	}

	/** The real top-level window, for Swing-only code that still needs it directly (e.g. dialogs, theme rebuild). */
	public JFrame getFrame() {
		return frame;
	}

	// ── Native peer creation ─────────────────────────────────────────────────────

	@Override
	public Object createNative(Control<?> control) {
		Object native_ =
			  control instanceof Button b      ? b.createSwingPeer()
			: control instanceof Label l       ? l.createSwingPeer()
			: control instanceof TextField t   ? t.createSwingPeer()
			: control instanceof TabbedPane tp ? tp.createSwingPeer()
			: control instanceof ScrollPane sp ? sp.createSwingPeer()
			: control instanceof SplitPane sp  ? sp.createSwingPeer()
			: control instanceof ToolBar tb    ? tb.createSwingPeer()
			: control instanceof Table t       ? t.createSwingPeer()
			: control instanceof Tree t        ? t.createSwingPeer()
			: control instanceof Panel p       ? p.createSwingPeer()
			: null;
		if (native_ != null) registerNative(control, native_);
		return native_;
	}

	// ── Bilateral sync ───────────────────────────────────────────────────────────

	@Override
	public void bind(Control<?> control, Object native_) {
		if (control instanceof Button b && native_ instanceof AbstractButton ab) {
			ActionListener l = e -> b.fireClicked();
			ab.addActionListener(l);
			unbindHooks.put(control, () -> ab.removeActionListener(l));
		} else if (control instanceof TextField t && native_ instanceof JTextField jt) {
			DocumentListener l = new DocumentListener() {
				@Override public void insertUpdate(DocumentEvent e)  { t.setTextFromNative(jt.getText()); }
				@Override public void removeUpdate(DocumentEvent e)  { t.setTextFromNative(jt.getText()); }
				@Override public void changedUpdate(DocumentEvent e) { t.setTextFromNative(jt.getText()); }
			};
			jt.getDocument().addDocumentListener(l);
			unbindHooks.put(control, () -> jt.getDocument().removeDocumentListener(l));
		}
	}

	@Override
	public void unbind(Control<?> control, Object native_) {
		Runnable hook = unbindHooks.remove(control);
		if (hook != null) hook.run();
		unregisterNative(native_);
	}

	@Override
	public void applyProperty(Control<?> control, String key, Object value) {
		Object native_ = control.getComponent();
		if (native_ == null) return;
		if (control instanceof Button && native_ instanceof AbstractButton ab) {
			switch (key) {
				case "text"    -> ab.setText((String) value);
				case "icon"    -> ab.setIcon((Icon) value);
				case "enabled" -> ab.setEnabled((Boolean) value);
			}
		} else if (control instanceof Label && native_ instanceof JLabel jl) {
			if ("text".equals(key)) jl.setText((String) value);
		} else if (control instanceof TextField && native_ instanceof JTextField jt) {
			if ("text".equals(key) && !jt.getText().equals(value)) jt.setText((String) value);
		}
	}

	// ── Tree mounting ─────────────────────────────────────────────────────────────

	@Override
	public void mount(Control<?> parent, Control<?> child, int index, Object constraints) {
		Object np = parent.getComponent();
		Object nc = child.getComponent();
		if (!(nc instanceof Component comp)) return;
		if (np instanceof JTabbedPane tp) {
			tp.addTab(constraints instanceof String s ? s : "", comp);
		} else if (np instanceof JScrollPane sp) {
			sp.setViewportView(comp);
		} else if (np instanceof Container c) {
			// Plain, unconstrained add — deliberately the single-arg overload (not
			// index-based): some layout managers special-case it (e.g. JSplitPane
			// auto-assigns the first two adds to left/right; addLayoutComponent
			// otherwise rejects a null constraint), matching what Control.add()
			// did directly before this abstraction existed.
			if (constraints != null) c.add(comp, constraints);
			else                     c.add(comp);
		}
	}

	@Override
	public void unmount(Control<?> parent, Control<?> child) {
		Object np = parent.getComponent();
		Object nc = child.getComponent();
		if (np instanceof JScrollPane sp && nc instanceof Component comp && sp.getViewport().getView() == comp) {
			sp.setViewportView(null);
		} else if (np instanceof Container c && nc instanceof Component comp) {
			c.remove(comp);
		}
	}
}
