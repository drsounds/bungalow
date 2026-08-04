package se.spacify.app.data.views;

import java.awt.Desktop;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import se.spacify.app.data.DataRepository;
import se.spacify.app.data.controller.DataController;
import se.spacify.app.spider.Postbacks;
import se.spacify.app.spider.Request;
import se.spacify.app.spider.Spider;
import se.spacify.navigation.TabBarView;
import se.spacify.navigation.ViewStack;

/**
 * The {@code spacify:table...} screen (index of custom tables, a table's row list,
 * a single row, or a row's related-rows view — see {@link DataController}). A
 * {@link TabBarView} rendering the controller's single {@code <page>} as its one tab,
 * exactly like {@code se.spacify.app.testapp.views.TestAppView}.
 *
 * <p>One postback convention is handled here rather than by the controller: a button
 * {@code onclick="nav:<target>"} is pure navigation, not a data mutation, so it never
 * reaches {@link DataController#data}. A {@code spacify:} target moves the view stack
 * (covering table/row/related links); anything else is treated as an external URI —
 * how a LINK field's hyperlink opens — and handed to the desktop's default browser.
 */
public class DataView extends TabBarView {

    private final Spider spider = new Spider();

    /** The URI last navigated to, replayed by postbacks that aren't a {@code nav:} action. */
    private String currentUri = "spacify:table";

    public DataView(ViewStack viewStack, DataRepository repo) {
        super(viewStack);
        spider.getControllers().add(new DataController(repo));
    }

    @Override
    public boolean acceptsUri(String uri) {
        return uri != null && uri.startsWith("spacify:table");
    }

    @Override
    public String getName() {
        return "Custom Tables";
    }

    @Override
    public void navigate(String uri) {
        currentUri = uri;
        render(new Request("GET", uri, new HashMap<>(), null, new HashMap<>()));
        super.navigate(uri);
    }

    /** Handle a button postback: a {@code nav:} action navigates, anything else re-renders in place. */
    private void refresh(String action) {
        if (action != null && action.startsWith("nav:")) {
            goTo(action.substring("nav:".length()));
            return;
        }
        Map<String, Object> headers = new HashMap<>();
        headers.put("action", action);
        render(new Request("POST", currentUri, headers, action, Postbacks.collectInput(getTabbedPane())));
    }

    /** Move the view stack for an in-app target, or open anything else in the system browser. */
    private void goTo(String target) {
        if (target == null || target.isEmpty()) {
            return;
        }
        if (target.startsWith("spacify:")) {
            getViewStack().navigate(target);
            return;
        }
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new java.net.URI(target));
            } catch (Exception ignored) {
                // Not a browsable URI (or no desktop support) — nothing sensible to do.
            }
        }
    }

    private void render(Request request) {
        Element view = spider.process(request);
        if (view == null) {
            return;
        }
        getTabbedPane().setInnerXul(view);
        Postbacks.bind(getTabbedPane(), this::refresh);
        getTabbedPane().revalidate();
        getTabbedPane().repaint();
    }
}
