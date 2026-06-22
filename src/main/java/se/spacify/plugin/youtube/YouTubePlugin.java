package se.spacify.plugin.youtube;

import java.util.List;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.plugin.Plugin;
import se.spacify.plugin.PluginContext;
import se.spacify.plugin.PluginSetting;

/**
 * Built-in plugin contributing the YouTube streaming Service. Registers a
 * {@link YouTubeMusicService} (the streaming aspect,
 * {@link se.spacify.plugin.media.service.MediaService}); registered after the local
 * music plugin so YouTube acts as a fallback for tracks not available locally.
 */
public class YouTubePlugin extends Plugin {

    public YouTubePlugin() {
        super();
    }
    @Override
    public void onActivate(PluginContext ctx) {
        ctx.registerService(new YouTubeMusicService(ctx.settings()));
    }

    /** Optional YouTube Data API key; when set, search uses the API (else a scrape). */
    @Override
    public List<PluginSetting> getSettingsSchema() {
        return List.of(PluginSetting.string(YouTubeMusicService.SETTING_API_KEY,
            "YouTube Data API key (optional)", ""));
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return "youtube";
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "YouTube";
    }

    @Override
    public void onRegister(AspectManager<? extends Aspect> aspectManager) {
        // TODO Auto-generated method stub
         
    }
}
