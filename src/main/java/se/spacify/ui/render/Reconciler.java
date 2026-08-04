package se.spacify.ui.render;

import se.spacify.controls.Control;
import se.spacify.ui.MainWindow;

/**
 * The middle layer that reconciles the {@code se.spacify.controls} tree with
 * the active {@link UserInterface}. {@link Control#add(Control)},
 * {@link Control#remove(Control)} and the XUL reconciler's replace-in-place
 * all funnel through here.
 *
 * <p>A backend whose widgets must be constructed already attached to a parent
 * (e.g. Jexer) can only create a control's native peer once that parent's own
 * peer exists, so a subtree is mounted strictly parent-first, retrying a
 * creation that wasn't possible earlier. But recursing into a child's own
 * children is only ever necessary for a child whose native peer we just had
 * to create here: a child that already had one — whether from Swing's eager,
 * self-mounting construction, or a control that wires its own children
 * directly (e.g. {@code SplitPane}) — is by construction already fully and
 * correctly mounted internally, and re-mounting its children again would
 * double-add them (fatal for a container with fixed slots, like
 * {@code JSplitPane}'s left/right).
 */
public final class Reconciler {

	private static final Reconciler INSTANCE = new Reconciler();

	public static Reconciler get() {
		return INSTANCE;
	}

	private Reconciler() {
	}

	/**
	 * Mount an entire subtree under the currently active backend. {@code root}
	 * must already carry its own native peer (set by the caller — typically a
	 * {@link UserInterface#start}). Called by a {@link UserInterface} from its
	 * own {@link UserInterface#start}.
	 */
	public void mountRoot(Control<?> root) {
		UserInterface ui = activeOrNull();
		if (ui == null) return;
		mountChildrenOf(ui, root);
	}

	public void onChildAdded(Control<?> parent, Control<?> child, int index, Object constraints) {
		UserInterface ui = activeOrNull();
		if (ui == null) return;
		mountChild(ui, parent, child, constraints);
	}

	public void onChildRemoved(Control<?> parent, Control<?> child) {
		UserInterface ui = activeOrNull();
		if (ui == null) return;
		if (ui.nativeFor(parent) != null && ui.nativeFor(child) != null) {
			ui.unmount(parent, child);
		}
	}

	public void onChildReplaced(Control<?> parent, Control<?> oldChild, Control<?> newChild, int index) {
		UserInterface ui = activeOrNull();
		if (ui == null) return;
		if (ui.nativeFor(parent) != null && ui.nativeFor(oldChild) != null) {
			ui.unmount(parent, oldChild);
		}
		mountChild(ui, parent, newChild, null);
	}

	/** Mount every one of {@code control}'s children into it (parent-first, recursively). */
	private void mountChildrenOf(UserInterface ui, Control<?> control) {
		for (Control<?> child : control.getChildren()) {
			mountChild(ui, control, child, null);
		}
	}

	private void mountChild(UserInterface ui, Control<?> parent, Control<?> child, Object constraints) {
		Object nativeParent = ui.nativeFor(parent);
		if (nativeParent == null) return;

		Object nativeChild = ui.nativeFor(child);
		boolean justCreated = false;
		if (nativeChild == null) {
			// The backend may not have been able to create this control's native
			// peer without knowing its parent (e.g. Jexer) — retry now that the
			// parent's peer exists (and is reachable via child.getParent()).
			nativeChild = ui.createNative(child);
			if (nativeChild != null) {
				child.adoptNative(nativeChild);
				ui.bind(child, nativeChild);
				justCreated = true;
			}
		}
		if (nativeChild == null) return;

		ui.mount(parent, child, parent.getChildren().indexOf(child), constraints);

		// child's own children could only be mounted once child itself had a
		// native peer — if it already had one, they already were (see class doc).
		if (justCreated) {
			mountChildrenOf(ui, child);
		}
	}

	private UserInterface activeOrNull() {
		MainWindow mw = MainWindow.getInstance();
		return mw != null ? mw.getUserInterfaceManager().getActive() : null;
	}
}
