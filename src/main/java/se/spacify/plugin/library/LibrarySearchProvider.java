package se.spacify.plugin.library;

import java.util.ArrayList;
import java.util.List;

import se.spacify.aspect.Aspect;
import se.spacify.aspect.AspectManager;
import se.spacify.db.DatabaseManager;
import se.spacify.db.LibraryRepository;
import se.spacify.db.entity.Artist;
import se.spacify.db.entity.Recording;
import se.spacify.db.entity.Release;
import se.spacify.db.entity.Track;
import se.spacify.search.EntityKind;
import se.spacify.search.SearchProvider;
import se.spacify.search.SearchResult;

/**
 * Searches the local library (SQLite) for matching artists, releases and songs.
 * Results navigate to the corresponding library views.
 */
public final class LibrarySearchProvider implements SearchProvider {

    private static final long LIMIT = 8;

    @Override public String getId()   { return "search.library"; }
    @Override public String getName() { return "Library"; }
    @Override public void onRegister(AspectManager<? extends Aspect> manager) {}

    @Override
    public List<SearchResult> search(String query) {
        List<SearchResult> out = new ArrayList<>();
        String like = "%" + query + "%";
        DatabaseManager db = DatabaseManager.getInstance();
        try {
            for (Artist a : db.artistDao().queryBuilder().limit(LIMIT).where().like("name", like).query()) {
                out.add(new SearchResult(EntityKind.ARTIST, a.getName(), "",
                    "spacify:library:artist:" + a.getId(), getName()));
            }
            for (Release r : db.releaseDao().queryBuilder().limit(LIMIT).where().like("title", like).query()) {
                out.add(new SearchResult(EntityKind.RELEASE, r.getTitle(),
                    LibraryRepository.artistNamesForRelease(r),
                    "spacify:library:release:" + r.getId(), getName()));
            }
            for (Recording rec : db.recordingDao().queryBuilder().limit(LIMIT).where().like("title", like).query()) {
                out.add(new SearchResult(EntityKind.RECORDING, rec.getTitle(),
                    LibraryRepository.artistNamesForRecording(rec), releaseUri(db, rec), getName()));
            }
        } catch (Exception ignored) {
            // DB error → whatever we collected so far.
        }
        return out;
    }

    /** Navigate a song to the album it appears on, when known. */
    private static String releaseUri(DatabaseManager db, Recording rec) {
        try {
            List<Track> tracks = db.trackDao().queryForEq("recording_id", rec.getId());
            if (!tracks.isEmpty() && tracks.get(0).getRelease() != null) {
                return "spacify:library:release:" + tracks.get(0).getRelease().getId();
            }
        } catch (Exception ignored) {}
        return null;
    }
}
