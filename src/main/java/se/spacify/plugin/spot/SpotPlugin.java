package se.spacify.plugin.spot;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.plugin.Plugin;
import se.spacify.plugin.PluginContext;
import se.spacify.plugin.spot.chrome.SpotChrome;
import se.spacify.plugin.spot.skin.SpotSkin;

/**
 * Built-in plugin contributing the YouTube streaming Service. Registers a
 * {@link YouTubeMusicService} (the streaming aspect,
 * {@link se.spacify.plugin.media.service.MediaService}); registered after the local
 * music plugin so YouTube acts as a fallback for tracks not available locally.
 */
public class SpotPlugin extends Plugin {

    @Override
    public void onActivate(PluginContext ctx) {
        ctx.registerChrome(new SpotChrome());
        ctx.registerDesign(new SpotDesign());
        ctx.registerSkin(new SpotSkin());
        ctx.registerTheme(new SpotTheme());
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "spot";
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Spot";
    }

    @Override
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // TODO Auto-generated method stub
    }
}
