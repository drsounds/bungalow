package se.spacify.dom;

import javax.swing.JFrame;

public class WindowElement extends Element {
    public WindowElement() {
        this.control = new JFrame();
    }
}
