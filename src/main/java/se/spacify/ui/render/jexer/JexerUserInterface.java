package se.spacify.ui.render.jexer;

import java.io.UnsupportedEncodingException;

import jexer.TAction;
import jexer.TApplication;
import jexer.TButton;
import jexer.TField;
import jexer.TLabel;
import jexer.TWidget;
import jexer.TWindow;

import se.spacify.controls.Button;
import se.spacify.controls.Control;
import se.spacify.controls.Label;
import se.spacify.controls.TextField;
import se.spacify.ui.render.Reconciler;
import se.spacify.ui.render.UserInterface;

/**
 * The alternate backend: renders the {@link Control} tree with Jexer, a
 * character-cell TUI toolkit. Only the controls ported to
 * {@link UserInterface} are supported ({@link Button}, {@link Label},
 * {@link TextField}, and whichever container hosts them as the root — see
 * {@link JexerHomeView}); everything else in {@code se.spacify.controls} is
 * Swing-only and simply isn't usable while this backend is active.
 *
 * <p>Spacify's pixel-level {@link se.spacify.skinning.Skin} painting has no
 * equivalent in a terminal; this backend renders with Jexer's own built-in
 * TUI theme rather than attempting to replicate it.
 *
 * <p>Jexer widgets are constructed already attached to a parent widget (there
 * is no separate "create, then add" step like Swing's), so
 * {@link #createNative} can only build a control's peer once its parent's own
 * peer exists — it returns {@code null} otherwise, and the
 * {@link Reconciler} retries once that becomes true.
 */
public class JexerUserInterface extends UserInterface {

	public static final String ID = "jexer";

	private TApplication app;
	private Thread appThread;

	public JexerUserInterface() {
		super(ID, "Jexer (Terminal)");
	}

	// ── Lifecycle ─────────────────────────────────────────────────────────────────

	@Override
	public void start(Control<?> root) {
		// Jexer's Swing-hosted backend builds its own frame via
		// SwingUtilities.invokeAndWait(), which throws if called from the EDT
		// itself — and start() typically is (MainWindow's constructor runs on the
		// EDT). So the whole TApplication — construction, not just run() — has to
		// happen on its own dedicated thread, exactly like Jexer's normal usage
		// pattern (analogous to Swing's own EDT).
		//
		// This must NOT block the calling (EDT) thread waiting for readiness:
		// TApplication's constructor itself calls invokeAndWait() into the EDT to
		// build its frame, so if the EDT were blocked here waiting on this thread,
		// neither side could ever make progress. start() is therefore
		// fire-and-forget; the Jexer window appears a moment after it returns.
		appThread = new Thread(() -> {
			try {
				app = new TApplication(TApplication.BackendType.SWING);
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException("Failed to start the Jexer backend", e);
			}
			TWindow window = app.addWindow("Spacify", 60, 20);
			root.adoptNative(window);
			registerNative(root, window);
			bind(root, window);
			Reconciler.get().mountRoot(root);
			app.run();
		}, "jexer-application");
		appThread.setDaemon(true);
		appThread.start();
	}

	@Override
	public void stop() {
		if (app != null) {
			app.exit();
			app = null;
			appThread = null;
		}
	}

	// ── Native peer creation ─────────────────────────────────────────────────────

	@Override
	public Object createNative(Control<?> control) {
		Control<?> parent = control.getParent();
		Object parentNative = parent != null ? nativeFor(parent) : null;
		if (!(parentNative instanceof TWidget parentWidget)) {
			return null; // parent not mounted yet — Reconciler retries once it is
		}

		// Stack children vertically, two rows apart, matching the VBox layout
		// JexerHomeView is built with.
		int row = parent.getChildren().indexOf(control);
		int y = row * 2 + 1;

		Object native_ =
			  control instanceof Button b
				? new TButton(parentWidget, orEmpty(b.getText()), 2, y, new TAction() {
					@Override public void DO() { b.fireClicked(); }
				})
			: control instanceof Label l
				? new TLabel(parentWidget, orEmpty(l.getText()), 2, y)
			: control instanceof TextField t
				? new TField(parentWidget, 2, y, 30, false, orEmpty(t.getText()))
			: null;
		if (native_ != null) registerNative(control, native_);
		return native_;
	}

	private static String orEmpty(String s) {
		return s != null ? s : "";
	}

	// ── Bilateral sync ───────────────────────────────────────────────────────────

	@Override
	public void bind(Control<?> control, Object native_) {
		if (control instanceof TextField t && native_ instanceof TField field) {
			field.setUpdateAction(new TAction() {
				@Override public void DO() { t.setTextFromNative(field.getText()); }
			});
		}
	}

	@Override
	public void unbind(Control<?> control, Object native_) {
		unregisterNative(native_);
	}

	@Override
	public void applyProperty(Control<?> control, String key, Object value) {
		Object native_ = control.getComponent();
		if (native_ == null) return;
		if (control instanceof Label && native_ instanceof TLabel label) {
			if ("text".equals(key)) label.setLabel((String) value);
		} else if (control instanceof TextField && native_ instanceof TField field) {
			if ("text".equals(key) && !field.getText().equals(value)) field.setText((String) value);
		}
		// TButton exposes no setter for its label in this Jexer version — a
		// button's text is fixed at creation for this backend.
	}

	// ── Tree mounting ─────────────────────────────────────────────────────────────

	@Override
	public void mount(Control<?> parent, Control<?> child, int index, Object constraints) {
		// No-op: Jexer widgets attach to their parent at construction time (see
		// createNative), so there is nothing further to attach here.
	}

	@Override
	public void unmount(Control<?> parent, Control<?> child) {
		Object native_ = child.getComponent();
		if (native_ instanceof TWidget w) w.remove();
	}
}
