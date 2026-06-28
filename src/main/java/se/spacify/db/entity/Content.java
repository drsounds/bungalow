package se.spacify.db.entity;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.ForeignCollectionField;

public class Content extends Node {
    @ForeignCollectionField(eager = false)
    private ForeignCollection<ArtistCredit> artistCredits;
    /**
     * Display-only artist credit for transient (catalogue / discovery) recordings
     * that aren't backed by DB {@link RecordingArtistCredit} rows. No
     * {@code @DatabaseField}, so ORMLite ignores it; persisted recordings carry
     * their artists in {@link #artistCredits} instead.
     */
    private String artistNames;
    public ForeignCollection<ArtistCredit> getArtistCredits() { return artistCredits; }
    public String    getArtistNames()          { return artistNames; }
    public void      setArtistNames(String v)  { this.artistNames = v; }

}
