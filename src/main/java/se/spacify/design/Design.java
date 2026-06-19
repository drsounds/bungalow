package se.spacify.design;

import se.spacify.skinning.Skin;

import se.spacify.ui.chrome.Chrome;

public interface Design { 
    public String getId();
    public Chrome getChrome();
    public Skin getSkin();
}
