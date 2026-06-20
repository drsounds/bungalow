package se.spacify.service.playlist;

import se.spacify.db.entity.Playable;

import java.util.List;

/**
 * A named, ordered collection of playable items. Kept deliberately minimal and
 * provider-agnostic so a {@link PlaylistService} backed by the local database, a
 * file, or a remote account can all expose playlists uniformly.
 */
public interface Playlist {

    /** Stable id, unique within its owning {@link PlaylistService}. */
    String getId();

    String getName();

    /** The playlist's items in order. */
    List<Playable> getItems();
}
