package se.spacify.plugin.spot;

import se.spacify.plugin.Plugin;
import se.spacify.plugin.PluginContext;
import se.spacify.plugin.spot.chrome.SpotChrome;
import se.spacify.plugin.spot.skin.SpotSkin;

/**
 * Built-in plugin contributing the YouTube streaming Service. Registers a
 * {@link YouTubeMusicService} (the streaming aspect,
 * {@link se.spacify.service.media.MediaService}); registered after the local
 * music plugin so YouTube acts as a fallback for tracks not available locally.
 */
public class SpotPlugin implements Plugin {

    @Override
    public void onActivate(PluginContext ctx) {
        ctx.registerChrome(new SpotChrome());
        ctx.registerDesign(new SpotDesign());
        ctx.registerSkin(new SpotSkin());
        ctx.registerTheme(new SpotTheme());
    }
}
