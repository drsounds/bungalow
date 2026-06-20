package se.spacify.plugin.wmp;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.plugin.Plugin;
import se.spacify.plugin.PluginContext;
import se.spacify.plugin.wmp.chrome.WMP1XChrome;
import se.spacify.plugin.wmp.chrome.WMP9Chrome;
import se.spacify.plugin.wmp.design.WMP1XDesign;
import se.spacify.plugin.wmp.skin.WMP10Skin;
import se.spacify.plugin.wmp.skin.WMP11Skin;
import se.spacify.plugin.wmp.skin.WMP8Skin;
import se.spacify.plugin.wmp.skin.WMP9Skin;

/**
 * Built-in plugin contributing the Windows Media Player look and feel: the WMP
 * Skins, Chromes, and the {@link WMP1XDesign} (the default Design, premixing the
 * WMP 1X Chrome with the WMP 9 Skin), all selectable from Settings.
 */
public class WMPPlugin extends Plugin {

    @Override
    public void onActivate(PluginContext ctx) {
    	ctx.registerSkin(new WMP8Skin());
    	ctx.registerSkin(new WMP9Skin());
    	ctx.registerSkin(new WMP10Skin());
    	ctx.registerSkin(new WMP11Skin());
    	ctx.registerChrome(new WMP9Chrome());
    	ctx.registerChrome(new WMP1XChrome());
    	ctx.registerDesign(new WMP1XDesign());
    }

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return "wmp";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Windows Media Player";
	}

	@Override
	public void onRegister(AspectManager<? extends Aspect> aspectManager) {
		// TODO Auto-generated method stub
	}
}
