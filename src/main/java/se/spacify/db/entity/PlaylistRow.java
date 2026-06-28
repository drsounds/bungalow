package se.spacify.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * One positioned entry in a {@link Playlist}. The content is referenced by a
 * {@code spacify:} URI rather than a hard foreign key, so a single playlist can
 * mix content of different types (recordings, and later video/game items) and
 * resolve each via the identifier/name lookup model.
 */
@DatabaseTable(tableName = "playlist_rows")
public class PlaylistRow extends ContentCollectionRow<Content<?>> {

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false, columnName = "playlist_id")
    private Playlist playlist;

    /** Denormalised playlist name, handy for flat listings. */
    @DatabaseField(canBeNull = true, columnName = "playlist_name")
    private String playlistName;

    /** spacify: URI of the referenced content. */
    @DatabaseField(canBeNull = false, columnName = "content_uri")
    private String contentUri;

    public PlaylistRow() {}

    public PlaylistRow(Playlist playlist, int position, String contentUri) {
        this.playlist   = playlist;
        this.contentUri = contentUri;
        this.playlistName = playlist != null ? playlist.getName() : null;
        setPosition(position);
    }

    public Playlist getPlaylist()           { return playlist; }
    public void     setPlaylist(Playlist v) { this.playlist = v; }
    public String   getPlaylistName()       { return playlistName; }
    public void     setPlaylistName(String v) { this.playlistName = v; }
    public String   getContentUri()         { return contentUri; }
    public void     setContentUri(String v) { this.contentUri = v; }
}
