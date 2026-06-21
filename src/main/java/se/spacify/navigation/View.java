package se.spacify.navigation;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import se.spacify.controls.Panel;

/**
 * A navigable screen. A View <em>is</em> a {@link Panel} (Swing component), so the
 * {@link ViewStack} mounts the View itself and shows/hides it automatically — no
 * manual component swapping. Subclasses render either directly into {@code this}
 * or, for legacy views, into an internal panel returned from {@link #getComponent()}
 * (the ViewStack wraps that inside this View on first show).
 */
public abstract class View extends Panel {

    private static final long serialVersionUID = 1L;

    private final ViewStack viewStack;
    private boolean mounted = false;

    public View(ViewStack viewStack) {
        this.viewStack = viewStack;
        setLayout(new BorderLayout());
    }

    public ViewStack getViewStack() {
        return viewStack;
    }

    public abstract boolean acceptsUri(String uri);

    public abstract void navigate(String uri);

    /**
     * The Swing component this view renders. A View is itself a {@link Panel}, so
     * this returns {@code this} by default; legacy views that build an internal
     * panel may override to return it.
     */
    public JComponent getComponent() {
        return this;
    }

    /**
     * Mount the view's content into itself once, so the ViewStack can show/hide the
     * View directly. Called by the {@link ViewStack} the first time the view is
     * shown. If {@link #getComponent()} returns {@code this} there is nothing to
     * wrap.
     */
    final void mount() {
        if (mounted) return;
        mounted = true;
        JComponent content = getComponent();
        if (content != null && content != this) {
            add(content, BorderLayout.CENTER);
        }
    }

    public String getTitle() {
        return "";
    }

    public void onShow() {}

    public void onHide() {}

    /**
     * Whether this view manages its own internal navigation history (e.g. an
     * embedded browser). When true, {@link ViewStack}'s back/forward delegate
     * to {@link #goBack()}/{@link #goForward()} before touching the view stack.
     */
    public boolean handlesHistory() { return false; }

    public boolean canGoBack()    { return false; }
    public boolean canGoForward() { return false; }
    public void    goBack()       {}
    public void    goForward()    {}
}
