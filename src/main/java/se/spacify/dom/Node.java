package se.spacify.dom;

public class Node {
    public Node() {
        attributes = new DOMNodeAttributeList(this);
    }
    public Node(String tagName) {
        this.tagName = tagName;
    }
    public Node createNode(String tagName) {
        Node node = new Node(tagName);
        return node;
    }
    protected String innerText;
    public String toXml() {

        StringBuilder sb = new StringBuilder();
        sb.append("<" + this.tagName + " ");
        for (DOMNodeAttribute attribute : attributes) {
            sb.append(" " + attribute.getId() + "=\"" + attribute.getValue().toString() + "\"");
        }
        sb.append(">");
        sb.append(this.getInnerXML());
        sb.append("</" + this.tagName + ">");
        return sb.toString();
    }
    public String getInnerXML() {
        StringBuilder sb = new StringBuilder();
        for (Node childNode : childNodes) {
            sb.append(childNode.toXml());
        }
        return sb.toString();
    }

    public Node(Node parentNode) {
        this.parentNode = parentNode;
    }
    protected void appendChild(Node node) {
        this.childNodes.add(node);
        node.parentNode = this;
    }
    protected void removeChild(Node node) {
        this.childNodes.remove(node);
        node.parentNode = null;
    }
    protected Node parentNode;
    public Node getParentNode() {
        return parentNode;
    }
    private String tagName = "";
    public String getTagName() {
        return tagName;
    }
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    protected DOMNodeList childNodes = new DOMNodeList();
    public DOMNodeList getChildNodes() {
        return childNodes;
    }
    protected DOMNodeAttributeList attributes;
    public DOMNodeAttributeList getAttributes() {
        return attributes;
    }
    public Object getAttribute(String attrName, Object defaultValue) {
        Object value = getAttribute(attrName);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
    public Object getAttribute(String attrName) {
        return getAttribute(attrName, null);
    }
}
