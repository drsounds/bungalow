package se.spacify.plugin.wmp;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.plugin.Plugin;
import se.spacify.plugin.PluginContext;
import se.spacify.plugin.wmp.chrome.WMP10Chrome;
import se.spacify.plugin.wmp.chrome.WMP9Chrome;
import se.spacify.plugin.wmp.skin.WMP10Skin;
import se.spacify.plugin.wmp.skin.WMP11Skin;
import se.spacify.plugin.wmp.skin.WMP8Skin;
import se.spacify.plugin.wmp.skin.WMP9Skin;

/**
 * Built-in plugin contributing the YouTube streaming Skin. Registers a
 * {@link YouTubeMusicSkin} (the streaming aspect,
 * {@link se.spacify.Skin.media.MediaSkin}); registered after the local
 * music plugin so YouTube acts as a fallback for tracks not available locally.
 */
public class WMPPlugin extends Plugin {

    @Override
    public void onActivate(PluginContext ctx) {
    	ctx.registerSkin(new WMP8Skin());
    	ctx.registerSkin(new WMP9Skin());
    	ctx.registerSkin(new WMP10Skin());
    	ctx.registerSkin(new WMP11Skin());
    	ctx.registerChrome(new WMP9Chrome());
    	ctx.registerChrome(new WMP10Chrome());
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
