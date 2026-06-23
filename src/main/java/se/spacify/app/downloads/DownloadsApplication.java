package se.spacify.app.downloads;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.app.Application;
import se.spacify.app.ApplicationContext;
import se.spacify.app.media.MediaDownloadScope;
import se.spacify.app.music.MusicDownloadScope;

/**
 * Built-in plugin contributing the Download Manager: the {@link DownloadService}
 * (and the JCEF download handler the store/web views attach), the
 * {@code spacify:downloads} {@link DownloadsView}, and the built-in audio capture
 * scopes from the music and media domains. The "Downloads" sidebar entry already
 * exists in the shell; this registers the view that answers it.
 *
 * <p>Capture scopes are registered as Services, so additional scopes (video,
 * images, …) can be added by any plugin implementing {@link DownloadCaptureScope}.
 * The music scope (specific extensions) is registered before the media scope
 * (broad {@code audio/*}) so the narrower filter matches first.
 */
public class DownloadsApplication extends Application {

    @Override public String getId()   { return "downloads"; }
    @Override public String getName() { return "Download Manager"; }
    @Override public void onRegister(AspectManager<? extends Aspect> manager) {}

    @Override
    public void onActivate(ApplicationContext ctx) {
        ctx.registerService(new DownloadService());
        ctx.registerService(new MusicDownloadScope());   // specific audio extensions (checked first)
        ctx.registerService(new MediaDownloadScope());   // broad audio/* fallback
        ctx.registerView(new DownloadsView(ctx.viewStack()));
    }
}
