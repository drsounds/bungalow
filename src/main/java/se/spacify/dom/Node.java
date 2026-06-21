package se.spacify.dom;

public interface Node {
    public void setAttribute(String attrName, Object value);
    public Object getAttribute(String attrName, Object defaultValue);
    public Object getAttribute(String attrName);
}
