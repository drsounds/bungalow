package se.spacify.plugin.localmusic;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.plugin.Plugin;
import se.spacify.plugin.PluginContext;
import se.spacify.service.media.LocalMusicService;

/**
 * Built-in plugin providing local file playback. Registers the
 * {@link LocalMusicService} (the music-streaming aspect) so the player bar,
 * now-playing queue and {@code PlaybackCoordinator} can resolve and play tracks.
 */
public class LocalMusicPlugin extends Plugin {

    @Override
    public void onActivate(PluginContext ctx) {
        ctx.registerService(new LocalMusicService());
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "localmusic";
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "Local Music";
    }

    @Override
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // TODO Auto-generated method stub
    }
}
