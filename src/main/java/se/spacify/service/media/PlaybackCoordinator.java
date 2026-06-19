package se.spacify.service.media;

import se.spacify.db.entity.LocalFile;
import se.spacify.db.entity.MusicServiceTrack;
import se.spacify.db.entity.Recording;
import se.spacify.service.ServiceManager;
import se.spacify.ui.ServiceMatchDialog;
import se.spacify.service.media.ServiceMatch;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Routes a library "play" request to a registered Service. It first asks every
 * {@link MusicService} to resolve the row by ISRC ({@link MusicService#lookup}),
 * then falls back to a title/artist metadata lookup
 * ({@link MusicService#lookupByTitleArtist}); the first Service that resolves
 * the request plays it. Also supports direct URI / local-file playback.
 *
 * <p>The Service that handled the most recent play is the <em>active</em> Service
 * ({@link #getActiveService()}); listeners ({@link #addActiveServiceListener})
 * are notified when it changes, so UI (the Now Playing panel, the footer) can
 * switch to that Service's player component and transport target.
 */
public final class PlaybackCoordinator {

    private PlaybackCoordinator() {}

    private static MediaService activeService;
    private static final List<Consumer<MediaService>> activeListeners = new ArrayList<>();

    /** The Service that handled the most recent play, or null if none yet. */
    public static MediaService getActiveService() { return activeService; }

    /** Subscribe to active-Service changes (fired on the calling/EDT thread). */
    public static void addActiveServiceListener(Consumer<MediaService> l) { activeListeners.add(l); }

    /** Record the active Service and notify listeners if it changed. */
    private static void setActiveService(MediaService s) {
        if (s == activeService) return;
        activeService = s;
        for (Consumer<MediaService> l : List.copyOf(activeListeners)) l.accept(s);
    }

    /**
     * Resolve a track across all registered Services, by ISRC first and then by
     * title/artist metadata, playing it on the first Service that resolves it.
     *
     * @return true if a Service handled the request.
     */
    public static boolean play(String isrc, String title, String artist) {
        return playByIsrc(isrc) || playByMetadata(title, artist);
    }

    /**
     * Look up the ISRC across all registered music Services and play it on the
     * first that resolves it.
     *
     * @return true if a Service handled the request.
     */
    public static boolean playByIsrc(String isrc) {
        if (isrc == null || isrc.isBlank()) return false;
        for (MusicService ms : ServiceManager.getInstance().getServices(MusicService.class)) {
            if (ms.lookup(isrc) != null) {
                setActiveService(ms);
                ms.loadByIsrc(isrc);
                ms.play();
                return true;
            }
        }
        return false;
    }

    /**
     * Look up by title/artist metadata across all registered music Services and
     * play it on the first that resolves it.
     *
     * @return true if a Service handled the request.
     */
    public static boolean playByMetadata(String title, String artist) {
        if (title == null || title.isBlank()) return false;
        for (MusicService ms : ServiceManager.getInstance().getServices(MusicService.class)) {
            if (ms.lookupByTitleArtist(title, artist) != null) {
                setActiveService(ms);
                ms.loadByTitleArtist(title, artist);
                ms.play();
                return true;
            }
        }
        return false;
    }

    /** Play an arbitrary spacify: URI on the primary media Service. */
    public static boolean playUri(String uri) {
        if (uri == null) return false;
        MediaService ms = ServiceManager.getInstance().getService(MediaService.class);
        if (ms == null) return false;
        setActiveService(ms);
        ms.loadUri(uri);
        ms.play();
        return true;
    }

    /** Play a local file directly via the local music Service. */
    public static boolean playLocalFile(LocalFile file) {
        LocalMusicService local = ServiceManager.getInstance().getService(LocalMusicService.class);
        if (local == null || file == null) return false;
        setActiveService(local);
        local.loadLocalFile(file);
        local.play();
        return true;
    }

    // ── "Play with…" resolution ─────────────────────────────────────────────────

    /**
     * Play {@code req}, honouring the user's saved "Play with…" choice. If a saved
     * {@link MusicServiceTrack} exists for this track it plays straight away on
     * that Service; otherwise the track is "unresolved" and the cross-Service
     * matches are gathered (off the EDT) and offered in the
     * {@link ServiceMatchDialog} so the user can choose — optionally remembering
     * the pick. Call on the EDT.
     */
    public static void resolveAndPlay(PlayRequest req) {
        resolveAndPlay(req, false);
    }

    /**
     * As {@link #resolveAndPlay(PlayRequest)}, but {@code forceChooser} skips any
     * saved choice and always shows the chooser — the "Play with…" / re-lookup
     * action behind the right-click menu.
     */
    public static void resolveAndPlay(PlayRequest req, boolean forceChooser) {
        if (req == null) return;
        if (!forceChooser) {
            MusicServiceTrack saved = ResolutionStore.find(req);
            if (saved != null && playSaved(saved)) return;
        }
        gatherAndChoose(req);
    }

    /** Replay a saved pick on its Service; false if that Service is gone. */
    private static boolean playSaved(MusicServiceTrack saved) {
        MusicService ms = findService(saved.getServiceId());
        if (ms == null) return false;
        setActiveService(ms);
        if (notBlank(saved.getMatchIsrc())) {
            ms.loadByIsrc(saved.getMatchIsrc());
        } else if (notBlank(saved.getMatchTitle())) {
            ms.loadByTitleArtist(saved.getMatchTitle(), saved.getMatchArtist());
        } else if (notBlank(saved.getMatchUri())) {
            ms.loadUri(saved.getMatchUri());
        } else {
            return false;
        }
        ms.play();
        return true;
    }

    /** Gather matches in the background, then offer the chooser (and play the pick). */
    private static void gatherAndChoose(PlayRequest req) {
        new SwingWorker<List<ServiceMatch>, Void>() {
            @Override protected List<ServiceMatch> doInBackground() { return gatherMatches(req); }

            @Override protected void done() {
                List<ServiceMatch> matches;
                try { matches = get(); } catch (Exception e) { matches = List.of(); }

                if (matches.isEmpty()) {
                    if (req.fallbackUri() != null) {
                        playUri(req.fallbackUri());
                    } else {
                        JOptionPane.showMessageDialog(null,
                            "No installed Service can play \"" + req.title() + "\".",
                            "Couldn't play track", JOptionPane.INFORMATION_MESSAGE);
                    }
                    return;
                }

                String label = req.title() + (req.artist().isBlank() ? "" : " — " + req.artist());
                ServiceMatchDialog.Result result = ServiceMatchDialog.choose(null, label, matches);
                if (result == null) return;          // cancelled
                playMatch(result.match(), req);
                if (result.always()) ResolutionStore.save(req, result.match());
            }
        }.execute();
    }

    /**
     * Ask every registered {@link MusicService} whether it can resolve {@code req}
     * — by ISRC first, then by title/artist — collecting one match per Service.
     * Performs blocking network I/O; never call on the EDT.
     */
    public static List<ServiceMatch> gatherMatches(PlayRequest req) {
        List<ServiceMatch> matches = new ArrayList<>();
        for (MusicService ms : ServiceManager.getInstance().getServices(MusicService.class)) {
            Recording match = null;
            boolean byIsrc = false;
            if (req.isrc() != null) {
                match = ms.lookup(req.isrc());
                if (match != null) byIsrc = true;
            }
            if (match == null && !req.title().isBlank()) {
                match = ms.lookupByTitleArtist(req.title(), req.artist());
            }
            if (match != null) matches.add(new ServiceMatch(ms, match, byIsrc));
        }
        return matches;
    }

    /** Load and play a chosen match on its Service. */
    private static void playMatch(ServiceMatch match, PlayRequest req) {
        MusicService ms = match.Service();
        setActiveService(ms);
        if (match.byIsrc() && req.isrc() != null) {
            ms.loadByIsrc(req.isrc());
        } else {
            ms.loadByTitleArtist(req.title(), req.artist());
        }
        ms.play();
    }

    private static MusicService findService(String ServiceId) {
        if (ServiceId == null) return null;
        for (MusicService ms : ServiceManager.getInstance().getServices(MusicService.class)) {
            if (ServiceId.equals(ms.getId())) return ms;
        }
        return null;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
