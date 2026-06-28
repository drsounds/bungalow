package se.spacify.db.entity;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.List;

/**
 * A named, ordered collection of mixed {@link Content} — the persistence behind a
 * user playlist. Being a {@link ContentCollection}, its entries ({@link PlaylistRow})
 * reference content by {@code spacify:} URI, so recordings, future video/game
 * items, etc. can be freely mixed in one playlist.
 *
 * <p>The resolved {@link Playable} items are held transiently in {@link #getItems()}
 * (populated by the playlist service on load); the durable rows live in
 * {@link #getRows()}.
 */
@DatabaseTable(tableName = "playlists")
public class Playlist extends ContentCollection<Content<?>> {

    @ForeignCollectionField(eager = false)
    private ForeignCollection<PlaylistRow> rows;

    /** Runtime, resolved items — not persisted directly (the rows are). */
    private final List<Playable> items = new ArrayList<>();

    public Playlist() {}

    public Playlist(String name) { setName(name); }

    public ForeignCollection<PlaylistRow> getRows() { return rows; }

    /** The resolved, ordered playable items for this playlist. */
    public List<Playable> getItems() { return items; }

    @Override public String toString() { return getName(); }
}
