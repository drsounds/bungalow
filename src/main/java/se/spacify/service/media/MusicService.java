package se.spacify.service.media;

import se.spacify.db.entity.Recording;

/**
 * Music-streaming aspect: ISRC- and title-based loading on top of the generic
 * {@link MediaService} playback aspect.
 */
public interface MusicService extends MediaService {

    /** Load and queue the recording identified by its ISRC code. */
    void loadByIsrc(String isrc);

    /** Best-effort lookup by title + artist name (fuzzy / exact depends on impl). */
    void loadByTitleArtist(String title, String artist);

    /**
     * Look up a Recording by ISRC without loading it for playback.
     * Returns null if not found.
     */
    Recording lookup(String isrc);

    /**
     * Best-effort lookup by title + artist without loading it for playback.
     * Returns null if not found. Default implementation finds nothing;
     * Services that can resolve metadata should override.
     */
    default Recording lookupByTitleArtist(String title, String artist) { return null; }
}
