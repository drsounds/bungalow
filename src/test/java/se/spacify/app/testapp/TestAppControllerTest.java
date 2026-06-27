package se.spacify.app.testapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.spacify.app.spider.Request;
import se.spacify.app.testapp.controller.TestController;
import se.spacify.net.Uri;

/**
 * Checks that {@link TestController} routes {@code spacify:testapp} and renders the
 * bundled {@code views/test.xml} template (loaded from the classpath).
 */
public class TestAppControllerTest {

    @Test
    public void acceptsTestAppUriAndRejectsOthers() throws URISyntaxException {
        TestController controller = new TestController();
        assertTrue(controller.acceptsUri(Uri.parse("spacify:testapp")));
        assertTrue(controller.acceptsUri(Uri.parse("spacify:testapp#overview")));
        assertFalse(controller.acceptsUri(Uri.parse("spacify:now-playing")));
    }

    @Test
    public void rendersBundledTemplateFromClasspath() {
        Request request = new Request("GET", "spacify:testapp", new HashMap<>(), null, new HashMap<>());

        Element view = new TestController().process(request);

        assertNotNull("template resource should load and render", view);
        assertEquals("view", view.getTagName());

        Element page = firstChild(view, "page");
        assertEquals("overview", page.getAttribute("id"));
        assertEquals("Overview", page.getAttribute("title"));

        // One date line plus the 1..10 loop.
        assertEquals(11, count(page, "text"));
        assertEquals(1, count(page, "button"));
    }

    private static Element firstChild(Element parent, String tag) {
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && ((Element) node).getTagName().equals(tag)) {
                return (Element) node;
            }
        }
        throw new AssertionError("no <" + tag + "> child");
    }

    private static int count(Element parent, String tag) {
        int n = 0;
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && ((Element) node).getTagName().equals(tag)) {
                n++;
            }
        }
        return n;
    }
}
