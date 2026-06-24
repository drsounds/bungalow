package se.spacify.dom;

import java.awt.Component;

import javax.swing.JComponent;

public class Element extends Node {
    public Element() {
        super();
    }
    public Element(Element parentElement) {
        this.parentElement = parentElement;
    }
    
    public void appendChild(Element elm) {
        this.appendChild(elm);
        if (control instanceof JComponent) {
            JComponent jControl = (JComponent)control;
            jControl.add(elm.getControl());
        }
    }
    public void removeChild(Element elm) {
        this.removeChild(elm);
        if (control instanceof JComponent) {
            JComponent jControl = (JComponent)control;
            jControl.remove(elm.getControl());
        }
    }

    protected Element parentElement;
    public Element getParentElement() {
        return parentElement;
    }
    
    protected Component control;
    public Component getControl() {
        return control;
    }
    public void setControl(Component control) {
        this.control = control;
    }
    public String getId() {
        return (String)getAttribute("id");
    }
    public DOMNodeList getElementsByTagName(String tagName) {
        DOMNodeList elements = new DOMNodeList();
        for (Node node : getChildNodes()) {
            if (node instanceof Element) {
                Element elm = (Element)node;
                if (getId().equals(tagName)) {
                    elements.add(elm); 
                }
                elements.addAll(elm.getElementsByTagName(tagName)); 
            }
        }
        return elements;
    }
    public Element getElementById(String id) {
        Element ret = null;
        for (Node node : getChildNodes()) {
            if (node instanceof Element) {
                Element elm = (Element)node;
                if (getId().equals(id)) {
                    ret = elm;
                    break;
                } else {
                    ret = elm.getElementById(id); 
                }                        
            }
        }
        return ret;
    }
}
