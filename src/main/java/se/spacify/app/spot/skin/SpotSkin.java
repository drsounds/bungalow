package se.spacify.app.spot.skin;

import java.awt.Color;
import java.awt.Paint;

import javax.swing.JComponent;

import se.spacify.controls.Table;
import se.spacify.skinning.Skin;

public class SpotSkin extends Skin {

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Spot";
    }

    @Override
    public Color getColorValue(JComponent comp, String key, Color defaultValue) {
        if (key.equals( "table.alternateBackground")) {
            System.out.println("table.alternateBackground");
            if (comp instanceof Table) {
                Table table = (Table)comp;
                if (table.getTaste().isDarkMode()) {                
                    return new Color(0, 0, 0, 61);
                } else {
                    return new Color(0, 0, 0,  11);

                }
            }
        }
        return defaultValue;
    }


    @Override
    public Paint getPaintValue(JComponent control, String key, Paint defaultValue) {
        if (key == "table.alternateBackground") {
            return new Color(0, 0, 0, 127);
        }
        return defaultValue;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "spot";
    }
    
}
