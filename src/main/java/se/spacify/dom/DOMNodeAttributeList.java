package se.spacify.dom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DOMNodeAttributeList extends ArrayList<DOMNodeAttribute> {
    private Node node;


    public Node getNode() {
        return node;
    }
    public DOMNodeAttributeList() {

    }
    public DOMNodeAttributeList(Node node) {
        this.node = node;
    }
    public DOMNodeAttribute get(Object attrName) {
        for (DOMNodeAttribute attrib : this) {
            if (attrib.getId() == attrName) {
                Object value = attrib.getValue();
                if (value instanceof String) {
                    if (!"".equals(value)) {
                        return attrib;
                    }
                } else {
                    if (value != null) {
                        return attrib;
                    }
                }
            }
        }
        return null;
    }

    public Object getValue(String attrName, Object defaultValue) {
        for (DOMNodeAttribute attrib : this) {
            if (attrib.getId() == attrName) {
                Object value = attrib.getValue();
                if (value instanceof String) {
                    if (!"".equals(value)) {
                        return attrib;
                    }
                } else {
                    if (value != null) {
                        return attrib;
                    }
                }
            }
        }
        return defaultValue;
    } 
    public boolean hasAttribute(String attrName) {
        return get(attrName) != null;
    }

    public DOMNodeAttribute put(String attrName, DOMNodeAttribute value) {
        DOMNodeAttribute attrib;
        if (hasAttribute(attrName)) {
            attrib = get(attrName);
        } else {
            attrib = new DOMNodeAttribute(attrName, value);
            this.add(attrib);
        }
        attrib.setValue(value);
        return value;
    }


    public boolean containsKey(Object key) {
        // TODO Auto-generated method stub
        return this.get(key) != null;
    }


    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        for (DOMNodeAttribute attrib : this) {
            if (attrib.getValue() == value) {
                return true;
            }
        }
        return false;
    }
  
    public void putAll(Map<? extends String, ? extends DOMNodeAttribute> m) {
        // TODO Auto-generated method stub
        for (String key : m.keySet()) {
            DOMNodeAttribute attrib = m.get(key);
            this.put(attrib.getId(), attrib);
        }
    }

    public Set<String> keySet() {
        // TODO Auto-generated method stub
        Set<String> ret = new HashSet<String>();
        for (DOMNodeAttribute attrib : this) {
            ret.add(attrib.getId());
        }
        return ret;
    }


    public Collection<DOMNodeAttribute> values() {
        // TODO Auto-generated method stub
        Collection<DOMNodeAttribute> ret = new HashSet<>();
        for (DOMNodeAttribute attrib : this) {
            ret.add(attrib);
        }
        return ret;
    }
}
