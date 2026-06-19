package se.spacify.navigation;

import javax.swing.JComponent;

public abstract class SPView {
    
    private SPViewStack viewStack;
    
    public SPViewStack getViewStack() {
        return viewStack;
    }

    public SPView(SPViewStack viewStack) {
        this.viewStack = viewStack;
    }

    public abstract boolean acceptsUri(String uri);

    public abstract void navigate(String uri);

    public abstract JComponent getComponent();

    public String getTitle() {
        return "";
    }

    public void onShow() {}

    public void onHide() {}

    /**
     * Whether this view manages its own internal navigation history (e.g. an
     * embedded browser). When true, {@link SPViewStack}'s back/forward delegate
     * to {@link #goBack()}/{@link #goForward()} before touching the view stack.
     */
    public boolean handlesHistory() { return false; }

    public boolean canGoBack()    { return false; }
    public boolean canGoForward() { return false; }
    public void    goBack()       {}
    public void    goForward()    {}
}
