package se.spacify.app.library.service;

import se.spacify.db.entity.Artist;
import se.spacify.db.entity.LocalFile;
import se.spacify.db.entity.Recording;
import se.spacify.db.entity.MusicRelease;
import se.spacify.db.entity.Track;
import se.spacify.service.Service;

import java.util.List;

/**
 * Library aspect: browsing a collection of music a user owns or has saved.
 * Implemented by Services that expose such a collection — the local catalogue,
 * or a remote account's saved library — and discovered via
 * {@link se.spacify.service.ServiceManager#getServices(Class)}. A Service may
 * combine this with the streaming aspect ({@link se.spacify.app.media.service.MediaService})
 * to both browse and play, or with the discovery aspect
 * ({@link se.spacify.app.catalogue.service.MusicCatalogueService}).
 */
public interface LibraryService extends Service {

    List<Artist>    getArtists();
    List<MusicRelease>   getReleases();
    List<Recording> getRecordings();
    List<Track>     getTracks();

    /** Locally-backed files, if this library has any; default none. */
    default List<LocalFile> getLocalFiles() { return List.of(); }

    /** Free-text search across the collection; default finds nothing. */
    default List<Recording> search(String query) { return List.of(); }
}
