package se.spacify.plugin.spot;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.plugin.Plugin;
import se.spacify.plugin.PluginContext;
import se.spacify.plugin.spot.chrome.Spot09Chrome;
import se.spacify.plugin.spot.design.Spot09Design;
import se.spacify.plugin.spot.skin.Spot09Skin;
import se.spacify.plugin.spot.skin.Spot15Skin;
import se.spacify.plugin.youtube.YouTubeMusicService;
 
/**
 * Built-in plugin contributing the YouTube streaming Service. Registers a
 * {@link YouTubeMusicService} (the streaming aspect,
 * {@link se.spacify.plugin.media.service.MediaService}); registered after the local
 * music plugin so YouTube acts as a fallback for tracks not available locally.
 */
public class SpotPlugin extends Plugin {

    @Override
    public void onActivate(PluginContext ctx) {
        ctx.registerChrome(new Spot09Chrome());
        ctx.registerDesign(new Spot09Design());
        ctx.registerSkin(new Spot09Skin());
        ctx.registerSkin(new Spot15Skin());
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
