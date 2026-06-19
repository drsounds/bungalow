package se.spacify.plugin.spot;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.design.Design;
import se.spacify.plugin.spot.skin.SpotSkin;
import se.spacify.plugin.spot.chrome.SpotChrome;
import se.spacify.skinning.Skin;

import se.spacify.ui.chrome.Chrome;

public class SpotDesign implements Design {
    private SpotSkin skin;
    private SpotChrome chrome;

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "spot";
    }

    @Override
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // TODO Auto-generated method stub
       
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "spot";
    }

    @Override
    public Chrome getChrome() {
        // TODO Auto-generated method stub
        return chrome == null ? chrome = new SpotChrome() : chrome;
    }

    @Override
    public Skin getSkin() {
        // TODO Auto-generated method stub
        return skin == null ? skin = new SpotSkin() : skin;
    }
    
}
