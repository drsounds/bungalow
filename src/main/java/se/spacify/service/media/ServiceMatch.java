package se.spacify.service.media;

import se.spacify.db.entity.Recording;
import se.spacify.plugin.music.service.MusicService;

import javax.swing.ImageIcon;

/**
 * A single candidate in the "Play with…" chooser: a {@link MusicService} that
 * reported it can play the requested track, together with the {@link Recording}
 * it matched (used both to show the user what was found and to replay the pick
 * later without re-searching).
 */
public final class ServiceMatch {

    private final MusicService Service;
    private final Recording    recording;
    private final boolean      byIsrc;

    public ServiceMatch(MusicService Service, Recording recording, boolean byIsrc) {
        this.Service   = Service;
        this.recording = recording;
        this.byIsrc    = byIsrc;
    }

    public MusicService Service()   { return Service; }
    public Recording    recording() { return recording; }

    /** Whether the Service matched on ISRC (vs. title/artist) — drives how it loads. */
    public boolean byIsrc() { return byIsrc; }

    public String    ServiceId()   { return Service.getId(); }
    public String    ServiceName() { return Service.getName(); }
    public ImageIcon icon()        { return Service.getServiceIcon(); }
}
