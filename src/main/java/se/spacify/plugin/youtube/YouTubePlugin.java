package se.spacify.plugin.youtube;

import se.spacify.plugin.Plugin;
import se.spacify.plugin.PluginContext;

/**
 * Built-in plugin contributing the YouTube streaming Service. Registers a
 * {@link YouTubeMusicService} (the streaming aspect,
 * {@link se.spacify.service.media.MediaService}); registered after the local
 * music plugin so YouTube acts as a fallback for tracks not available locally.
 */
public class YouTubePlugin implements Plugin {

    @Override
    public void onActivate(PluginContext ctx) {
        ctx.registerService(new YouTubeMusicService());
    }
}
