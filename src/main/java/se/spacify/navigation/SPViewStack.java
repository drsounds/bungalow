package se.spacify.navigation;

import se.spacify.ui.MainWindow;
import se.spacify.controls.Panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class SPViewStack extends Panel {

    private static final long serialVersionUID = 6893909424664812987L;
	private final List<SPView> registeredViews = new ArrayList<>();
    private final Deque<String> backStack = new ArrayDeque<>();
    private final Deque<String> forwardStack = new ArrayDeque<>();
    private final List<NavigationListener> listeners = new ArrayList<>();

    private String currentUri = null;
    private SPView currentView = null;
    
    public MainWindow getMainWindow() {
    	Component parent = getParent();
    	while (parent != null && parent != this) {
    		if (parent instanceof MainWindow) {
    			return (MainWindow)parent;
    		}
    		parent = parent.getParent();
    	}
    	return null;
    }

    public SPViewStack() {
        setLayout(new BorderLayout());
    }

    public void registerView(SPView view) {
        registeredViews.add(view);
    }

    /**
     * Remove a previously-registered view (e.g. when a plugin is disabled). If it
     * is currently showing, navigate away to a safe default first so the stack
     * isn't left displaying an orphaned component.
     */
    public void unregisterView(SPView view) {
        registeredViews.remove(view);
        if (view == currentView) {
            currentView.onHide();
            remove(currentView.getComponent());
            currentView = null;
            currentUri = null;
            revalidate();
            repaint();
            navigate("spacify:now-playing");
        }
    }

    public void navigate(String uri) {
        navigate(uri, true);
    }

    private void navigate(String uri, boolean pushHistory) {
        if (uri == null || uri.equals(currentUri)) return;

        SPView matched = null;
        for (SPView v : registeredViews) {
            if (v.acceptsUri(uri)) {
                matched = v;
                break;
            }
        }
        if (matched == null) return;

        if (pushHistory && currentUri != null) {
            backStack.push(currentUri);
            forwardStack.clear();
        }

        // Same view, different URI: retarget it in place rather than swapping
        // components (important for heavyweight views such as the web view).
        if (matched == currentView) {
            currentUri = uri;
            currentView.navigate(uri);
            currentView.onShow();
            notifyListeners();
            return;
        }

        if (currentView != null) {
            currentView.onHide();
            remove(currentView.getComponent());
        }

        currentUri = uri;
        currentView = matched;
        currentView.navigate(uri);
        currentView.onShow();

        add(currentView.getComponent(), BorderLayout.CENTER);
        revalidate();
        repaint();

        notifyListeners();
    }

    public void back() {
        // A view with its own history (e.g. the web view) consumes back first.
        if (currentView != null && currentView.handlesHistory() && currentView.canGoBack()) {
            currentView.goBack();
            notifyListeners();
            return;
        }
        if (backStack.isEmpty()) return;
        String prev = backStack.pop();
        if (currentUri != null) forwardStack.push(currentUri);
        navigate(prev, false);
    }

    public void forward() {
        if (currentView != null && currentView.handlesHistory() && currentView.canGoForward()) {
            currentView.goForward();
            notifyListeners();
            return;
        }
        if (forwardStack.isEmpty()) return;
        String next = forwardStack.pop();
        if (currentUri != null) backStack.push(currentUri);
        navigate(next, false);
    }

    public boolean canGoBack() {
        if (currentView != null && currentView.handlesHistory() && currentView.canGoBack()) return true;
        return !backStack.isEmpty();
    }

    public boolean canGoForward() {
        if (currentView != null && currentView.handlesHistory() && currentView.canGoForward()) return true;
        return !forwardStack.isEmpty();
    }

    public String getCurrentUri() {
        return currentUri;
    }

    /**
     * Reflect a navigation that happened inside the current view (e.g. a link
     * click in the web view) into the address field and nav buttons, without
     * re-dispatching to the view (which would reload it).
     */
    public void updateCurrentUri(String uri) {
        if (uri == null || uri.equals(currentUri)) { refreshNavState(); return; }
        currentUri = uri;
        notifyListeners();
    }

    /** Re-emit the current nav state to listeners (e.g. to refresh back/forward). */
    public void refreshNavState() {
        notifyListeners();
    }

    public void addNavigationListener(NavigationListener l) {
        listeners.add(l);
    }

    private void notifyListeners() {
        for (NavigationListener l : listeners) {
            l.onNavigate(currentUri, canGoBack(), canGoForward());
        }
    }
}
