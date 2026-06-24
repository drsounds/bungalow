package se.spacify.dom;

public class DOMNodeAttribute {
    private DOMNodeAttributeList parent;
    public DOMNodeAttributeList getParent() {
        return parent;
    }
    public DOMNodeAttribute() {

    }
    public DOMNodeAttribute(String id, Object value, DOMNodeAttributeList parent) {
        this.id = id;
        this.value = value;
        this.parent = parent;
    }
    private String id;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    private Object value;
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }    
}
