package se.spacify.app.localmusic;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.app.Application;
import se.spacify.app.ApplicationContext;
import se.spacify.app.localmusic.service.LocalMusicService;

/**
 * Built-in plugin providing local file playback. Registers the
 * {@link LocalMusicService} (the music-streaming aspect) so the player bar,
 * now-playing queue and {@code PlaybackCoordinator} can resolve and play tracks.
 */
public class LocalMusicApplication extends Application {

    @Override
    public void onActivate(ApplicationContext ctx) {
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
