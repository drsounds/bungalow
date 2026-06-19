package se.spacify.plugin.spot;

import se.spacify.design.Design;
import se.spacify.plugin.spot.skin.SpotSkin;
import se.spacify.plugin.spot.chrome.SpotChrome;
import se.spacify.skinning.Skin;
import se.spacify.ui.MainWindow;
import se.spacify.ui.chrome.Chrome;

public class SpotDesign implements Design {

    @Override
    public Skin createSkin() {
        // TODO Auto-generated method stub
        return new SpotSkin();
    }

    @Override
    public Chrome createChrome() {
        return new SpotChrome();
    }

    @Override
    public void apply(MainWindow mainWindow) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'apply'");
    }
    
}
