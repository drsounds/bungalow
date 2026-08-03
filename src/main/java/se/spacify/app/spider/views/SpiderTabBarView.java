package se.spacify.app.spider.views;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import se.spacify.app.spider.Postbacks;
import se.spacify.app.spider.Request;
import se.spacify.app.spider.Spider;

import se.spacify.navigation.TabBarView;
import se.spacify.navigation.ViewStack;

public abstract class SpiderTabBarView extends TabBarView {

    private final Spider spider = new Spider();

    /** The URI last navigated to, replayed by refresh postbacks. */
    private String currentUri;

    public SpiderTabBarView(ViewStack viewStack, String currentUri) {
        super(viewStack);
        this.currentUri = currentUri;
    }

    @Override
    public boolean acceptsUri(String uri) {
        return uri != null && uri.startsWith(currentUri);
    }
 
    @Override
    public void navigate(String uri) {
        currentUri = uri;
        render(new Request("GET", uri, new HashMap<>(), null, new HashMap<>()));
        // Honour a #fragment by switching to the tab with that id (TabBarView).
        super.navigate(uri);
    }

    /** Re-render after a button postback, replaying the current URI as a {@code POST}. */
    private void refresh(String action) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("action", action);
        render(new Request("POST", currentUri, headers, action, Postbacks.collectInput(getTabbedPane())));
    }

    /** Process {@code request} through the spider and render its pages as tabs. */
    private void render(Request request) {
        Element view = spider.process(request);
        if (view == null) {
            return;
        }
        // setInnerXul renders the root's children: the <view>'s <page>s become tabs.
        getTabbedPane().setInnerXul(view);
        Postbacks.bind(getTabbedPane(), this::refresh);
        getTabbedPane().revalidate();
        getTabbedPane().repaint();
    }
}
