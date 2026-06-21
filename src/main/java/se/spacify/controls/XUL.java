package se.spacify.controls;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import se.spacify.dom.Element;

/**
 * A XUL-style element: an XML node that is also its own Swing component (a
 * {@link Panel}), so a markup tree builds a live UI tree. This is the Java/Swing
 * translation of the C++ {@code SPElement} parser that used {@code wxXml}; here
 * {@code wxXmlNode} becomes the Java-standard {@link org.w3c.dom} API and each
 * tag maps to one of the project's Swing controls.
 *
 * <pre>{@code
 *   XUL ui = XUL.parse("<vbox><text id='t'>Hello</text><button onclick='go'>OK</button></vbox>");
 *   add(ui);
 * }</pre>
 */
public class XUL extends Panel implements Element {

    private final Map<String, Object> attributes = new HashMap<>();
    /** Logical child elements (mirrors the C++ {@code children} vector). */
    private final List<XUL> childElements = new ArrayList<>();
    private String tagName;
    private JComponent content;     // hosted widget for leaf tags; null for plain containers
    private XUL parentElement;

    public XUL() {
        // Containers stack their children vertically by default; leaves and the
        // view/splitter tags override this in createElement().
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setOpaque(false);
    }

    // ── Node / Element: attribute bag ───────────────────────────────────────────

    @Override
    public void setAttribute(String attrName, Object value) {
        attributes.put(attrName, value);
    }

    @Override
    public Object getAttribute(String attrName, Object defaultValue) {
        Object value = attributes.get(attrName);
        return value != null ? value : defaultValue;
    }

    @Override
    public Object getAttribute(String attrName) {
        return getAttribute(attrName, null);
    }

    /** String view of an attribute (C++ {@code getStringAttribute}). */
    public String getStringAttribute(String attrName, String defaultValue) {
        Object value = attributes.get(attrName);
        return value != null ? String.valueOf(value) : defaultValue;
    }

    /**
     * Depth-first search by the {@code id} attribute. Translation of the C++
     * {@code SPElement::getElementById}, walking the logical children.
     */
    @Override
    public Element getElementById(String id) {
        for (XUL child : childElements) {
            if (id.equals(child.getStringAttribute("id", null))) {
                return child;
            }
            Element grandChild = child.getElementById(id);
            if (grandChild != null) {
                return grandChild;
            }
        }
        return null;
    }

    // ── SPElement-equivalent helpers (the element is a plain JComponent) ─────────

    public String getTagName()          { return tagName; }
    public void   setTagName(String t)  { this.tagName = t; }
    /** C++ {@code getType()}. */
    public String getType()             { return tagName; }
    public XUL    getParentElement()    { return parentElement; }
    public JComponent getContent()      { return content; }
    /** Where children are appended; here the element itself (C++ {@code getSlotElement}). */
    public XUL    getSlotElement()      { return this; }

    /** Store an attribute and reflect the well-known ones onto the hosted widget. */
    public void set(String attrName, String value) {
        setAttribute(attrName, value);
        switch (attrName) {
            case "label", "value", "text" -> setInnerText(value);
            case "src" -> { if (content instanceof JLabel l) l.setIcon(new ImageIcon(value)); }
            default -> { /* opaque attribute */ }
        }
    }

    /** Push text content into the hosted widget (C++ {@code setInnerText}). */
    public void setInnerText(String text) {
        if (content instanceof AbstractButton b)       b.setText(text);
        else if (content instanceof JLabel l)          l.setText(text);
        else if (content instanceof JTextComponent t)  t.setText(text);
    }

    /**
     * Register a DOM-style event listener. Only {@code "click"} on a button maps
     * to a Swing {@link AbstractButton} action (C++ {@code addEventListener}).
     */
    public void addEventListener(String type, Consumer<XUL> handler) {
        if ("click".equals(type) && content instanceof AbstractButton b) {
            b.addActionListener(e -> handler.accept(this));
        }
    }

    /** Append a child element into the slot (C++ {@code appendChild}). */
    public XUL appendChild(XUL child) {
        childElements.add(child);
        child.parentElement = this;
        if ("splitter".equals(tagName) && content instanceof SplitPane sp) {
            if (sp.getLeftComponent() == null) sp.setLeftComponent(child);
            else                               sp.setRightComponent(child);
        } else if ("view".equals(tagName) && content instanceof TabbedPane tp) {
            tp.addTab(child.getStringAttribute("label", child.getStringAttribute("id", "")), child);
        } else {
            add(child);
        }
        return child;
    }

    /** Swing equivalent of wx {@code pack()}: re-layout this subtree. */
    public void pack() {
        revalidate();
        repaint();
    }

    // ── Parsing: wxXml → org.w3c.dom ─────────────────────────────────────────────

    /** Parse a XUL document from a markup string and return its root element. */
    public static XUL parse(String xml) throws Exception {
        return parse(new InputSource(new StringReader(xml)));
    }

    /** Parse a XUL document from a stream and return its root element. */
    public static XUL parse(InputStream in) throws Exception {
        return parse(new InputSource(in));
    }

    private static XUL parse(InputSource source) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        Document doc = factory.newDocumentBuilder().parse(source);
        XUL document = new XUL();
        document.setTagName("#document");
        return document.parseElement(doc.getDocumentElement(), document, document);
    }

    /**
     * Build the element subtree for {@code xmlNode}. Translation of the C++
     * {@code SPElement::parseElement}: {@code wxXmlNode} → {@link org.w3c.dom.Element},
     * attribute/child iteration via the DOM API, and the {@code this} pack() calls
     * preserved.
     */
    public XUL parseElement(org.w3c.dom.Element xmlNode, XUL parent, XUL document) {
        XUL elm = createElement(xmlNode.getTagName(), parent);
        assert elm.getParentElement() != null;
        elm.setVisible(true);

        // for (attribute = node.GetAttributes(); attribute; attribute = attribute.GetNext())
        NamedNodeMap attrs = xmlNode.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attribute = attrs.item(i);
            String name = attribute.getNodeName();
            String value = attribute.getNodeValue();
            elm.set(name, value);
            if ("hidden".equals(name)) {
                elm.setVisible(false);
            }
            if ("onclick".equals(name) && "button".equals(elm.getType())) {
                elm.addEventListener("click", document::onPostBackButtonClick);
            }
        }

        // for (child = node.GetChildren(); child; child = child.GetNext())
        NodeList children = xmlNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getParentNode() != xmlNode) {
                break;
            }
            if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getNodeValue();
                if (text != null && !text.isBlank()) {
                    elm.getSlotElement().setInnerText(text.trim());
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                XUL childElement = parseElement((org.w3c.dom.Element) child, elm.getSlotElement(), document);
                elm.getSlotElement().appendChild(childElement);
                this.pack();
            }
        }

        elm.pack();
        return elm;
    }

    /**
     * Map a tag name to a Swing control (C++ {@code SPElement::createElement}).
     * Leaf tags host a widget; container tags lay their children out themselves.
     */
    protected XUL createElement(String tagName, XUL parent) {
        XUL elm = new XUL();
        elm.parentElement = parent;
        switch (tagName) {
            case "text"     -> elm.setContent(new Label());          // ~ SPTextElement
            case "img"      -> elm.setContent(new Label());          // ~ SPImageElement (src → icon)
            case "button"   -> elm.setContent(new GlossyButton());   // ~ SPButtonElement
            case "input"    -> elm.setContent(new TextField());      // ~ SPInputElement
            case "view"     -> elm.setContent(new TabbedPane());     // ~ SPTabBarViewElement
            case "splitter" -> elm.setContent(new SplitPane());      // ~ SPSplitterElement
            case "vbox"     -> elm.setLayout(new BoxLayout(elm, BoxLayout.PAGE_AXIS)); // ~ SPVBoxElement
            case "hbox"     -> elm.setLayout(new BoxLayout(elm, BoxLayout.LINE_AXIS)); // ~ SPHBoxElement
            case "section"  -> elm.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)); // ~ SPSectionElement
            default         -> { /* plain SPElement */ }
        }
        elm.setTagName(tagName);
        return elm;
    }

    /** Host a single widget as this element's content (leaf / view / splitter). */
    private void setContent(JComponent widget) {
        this.content = widget;
        removeAll();
        setLayout(new java.awt.BorderLayout());
        add(widget, java.awt.BorderLayout.CENTER);
    }

    /**
     * Called when an element declaring {@code onclick} is clicked. Override to
     * handle the postback (C++ {@code onPostBackButtonClick}); no-op by default.
     */
    protected void onPostBackButtonClick(XUL elm) {
        // no-op
    }
}
