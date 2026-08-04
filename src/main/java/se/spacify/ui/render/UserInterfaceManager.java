package se.spacify.ui.render;

import se.spacify.aspect.BaseAspectManager;
import se.spacify.controls.Control;
import se.spacify.ui.MainWindow;

/**
 * Registry of the {@link UserInterface} backends available at runtime (Swing,
 * Jexer, …), plus which one is currently active. New controls delegate their
 * native-peer creation to {@link #getActive()}; switching backends
 * ({@link #switchTo}) tears down the previously-active backend's top-level
 * window and starts the new one against a (possibly different) root control.
 */
public class UserInterfaceManager extends BaseAspectManager<UserInterface> {

	private UserInterface active;

	/** Backend for controls built with no live {@link MainWindow} (e.g. unit tests); always Swing. */
	private static UserInterface standalone;

	public UserInterfaceManager(MainWindow mainWindow) {
		super(mainWindow);
	}

	/**
	 * The backend a {@link Control} falls back to when constructed outside any
	 * {@link MainWindow} — controls have always been constructible in isolation
	 * (e.g. in a unit test), so this keeps that true instead of requiring a full
	 * app bootstrap just to build one.
	 */
	public static UserInterface standalone() {
		if (standalone == null) {
			standalone = new se.spacify.ui.render.swing.SwingUserInterface();
		}
		return standalone;
	}

	/**
	 * The backend new controls currently delegate native-peer creation to.
	 * Defaults to whichever backend was registered first (Swing).
	 */
	public UserInterface getActive() {
		if (active == null) {
			active = all().stream().findFirst().orElse(null);
		}
		return active;
	}

	/**
	 * Switch which backend new controls delegate to, without starting or
	 * stopping any top-level window. Used during boot, before the initial
	 * {@link UserInterface#start} call, to make sure controls built for an
	 * alternate backend (e.g. a Jexer-only view) are created against it.
	 */
	public void setActiveId(String id) {
		UserInterface next = get(id);
		if (next != null) active = next;
	}

	/**
	 * Runtime switch: stop the currently active backend's top-level window (if
	 * one was started) and start {@code id} against {@code root}.
	 */
	public void switchTo(String id, Control<?> root) {
		UserInterface next = get(id);
		if (next == null || next == active) return;
		if (active != null) active.stop();
		active = next;
		active.start(root);
	}
}
