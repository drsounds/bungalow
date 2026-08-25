package se.spacify.controls;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A dropdown/select control wrapping a {@link JComboBox}, populated from
 * {@code <option value="..." selected="true">Label</option>} children — the XUL
 * counterpart to an HTML {@code <select>}. Unlike every other {@link Control}, its
 * children are pure data (the option list), not mounted widgets, so
 * {@link #setInnerXul(Element)} parses them directly instead of going through the
 * generic child-reconciliation {@link Control#setInnerXul(Element)} otherwise does.
 */
public class SelectField extends Control<JComboBox<SelectField.Option>> {

    public record Option(String value, String label) {
        @Override public String toString() { return label; }
    }

    public SelectField() {
        this.component = new JComboBox<>();
    }

    /** The {@code value} of the currently selected option, or {@code null} if none. */
    public String getSelectedValue() {
        Option o = component.getItemAt(component.getSelectedIndex());
        return o != null ? o.value() : null;
    }

    @Override
    public void setInnerXul(Element root) {
        List<Option> options = new ArrayList<>();
        int selectedIndex = -1;
        NodeList kids = root.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            Node node = kids.item(i);
            if (!(node instanceof Element el) || !"option".equals(el.getTagName())) {
                continue;
            }
            options.add(new Option(el.getAttribute("value"), el.getTextContent().trim()));
            if ("true".equalsIgnoreCase(el.getAttribute("selected"))) {
                selectedIndex = options.size() - 1;
            }
        }
        component.setModel(new DefaultComboBoxModel<>(options.toArray(new Option[0])));
        if (selectedIndex >= 0) {
            component.setSelectedIndex(selectedIndex);
        }
    }
}
