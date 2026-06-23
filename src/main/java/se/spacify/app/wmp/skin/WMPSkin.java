package se.spacify.app.wmp.skin;

import java.awt.Color;
import java.awt.Paint;

import javax.swing.JComponent;

import se.spacify.controls.Control;
import se.spacify.skinning.Skin;

public class WMPSkin extends Skin {

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "WMP";
    }

    @Override
    public Paint getPaintValue(JComponent component, String key, Paint defaultValue) {
        Control control = (Control)component;
        if (key == "table.alternateBackground") {
            if (control.getTaste().isDarkMode()) {
                return new Color(255, 255, 255, 25);
            } else {
                return new Color(0, 0, 0, 1);
            }
        }
        return defaultValue;
    }

    @Override
    public Color getColorValue(JComponent component, String key, Color defaultValue) {
        if (component instanceof Control) {
            Control control = (Control)component;
            if (key == "table.alternateBackground") {
                if (control.getTaste().isDarkMode()) {
                    return new Color(255, 255, 255, 25);
                } else {
                    return new Color(0, 0, 0,  15);
                }
            }
        }
        return defaultValue;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "wmp";
    }
    
}
