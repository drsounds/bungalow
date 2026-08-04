package se.spacify.ui.render;

import java.util.IdentityHashMap;
import java.util.Map;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.controls.Control;

/**
 * One implementation per UI toolkit (Swing, Jexer, …). A {@code UserInterface}
 * is the sole bridge between the toolkit-agnostic {@link Control} tree in
 * {@code se.spacify.controls} and a specific toolkit: it creates each
 * control's native peer, keeps the control's own getters/setters and the
 * native peer's state in sync bilaterally, and mounts/unmounts native
 * children as the {@link Control} tree mutates (via {@link Reconciler}).
 * Registered with, and selected at runtime through,
 * {@link UserInterfaceManager}.
 */
public abstract class UserInterface implements Aspect {

	private final String id;
	private final String name;

	/** Native peer -> owning control, so event handlers can route back to the Control that fired them. */
	private final Map<Object, Control<?>> nativeToControl = new IdentityHashMap<>();

	protected UserInterface(String id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override public String getId() { return id; }
	@Override public String getName() { return name; }
	@Override public void onRegister(AspectManager<? extends Aspect> aspectManager) { }

	// ── Lifecycle: the real top-level native window/application ─────────────────

	/**
	 * Create this backend's top-level native window/application and mount
	 * {@code root} into it. {@code root}'s subtree may already have native peers
	 * (e.g. it was mounted by this same backend before and is being re-shown); a
	 * peer is only created for a control that doesn't have one yet.
	 */
	public abstract void start(Control<?> root);

	/** Tear down the top-level window/application (e.g. before switching backends). */
	public abstract void stop();

	// ── Native peer creation and bilateral sync ──────────────────────────────────

	/**
	 * Create (but do not mount) the native peer for {@code control} — e.g.
	 * {@code new JButton()} for Swing, a {@code TButton} for Jexer — using
	 * whatever backend-specific factory the concrete {@link Control} subclass
	 * exposes. Returns {@code null} if this backend has no mapping for
	 * {@code control}'s class (e.g. a control that hasn't been ported to this
	 * backend yet).
	 */
	public abstract Object createNative(Control<?> control);

	/**
	 * Install native → control listeners (e.g. a document listener that pushes
	 * typed text back into a {@code TextField}) so the two sides of a control's
	 * state stay in sync bilaterally — the control → native direction is handled
	 * by {@link #applyProperty}.
	 */
	public abstract void bind(Control<?> control, Object native_);

	/** Undo {@link #bind}, releasing any native listeners installed for {@code control}. */
	public abstract void unbind(Control<?> control, Object native_);

	/** Push a single control-level property change (e.g. {@code "text"}) into its native peer. */
	public abstract void applyProperty(Control<?> control, String key, Object value);

	// ── Tree mounting ─────────────────────────────────────────────────────────────

	/**
	 * Attach {@code child}'s native peer into {@code parent}'s, at {@code index}
	 * among the parent's mounted children. {@code constraints} carries a
	 * toolkit-specific layout hint (e.g. a {@code BorderLayout} side) when the
	 * control tree was built with {@link Control#add(Control, Object)}; backends
	 * that have no notion of layout constraints ignore it.
	 */
	public abstract void mount(Control<?> parent, Control<?> child, int index, Object constraints);

	/** Detach {@code child}'s native peer from {@code parent}'s. */
	public abstract void unmount(Control<?> parent, Control<?> child);

	// ── Native <-> Control lookup ──────────────────────────────────────────────────

	/** The native peer currently backing {@code control}, or {@code null} if none has been created. */
	public final Object nativeFor(Control<?> control) {
		return control.getComponent();
	}

	/** The control that owns {@code native_} (see {@link #registerNative}), or {@code null} if none. */
	public final Control<?> controlFor(Object native_) {
		return nativeToControl.get(native_);
	}

	/** Backends call this once a native peer is created, so {@link #controlFor} can find it again. */
	protected final void registerNative(Control<?> control, Object native_) {
		nativeToControl.put(native_, control);
	}

	/** Backends call this when a native peer is discarded (e.g. in {@link #unbind}). */
	protected final void unregisterNative(Object native_) {
		nativeToControl.remove(native_);
	}
}
