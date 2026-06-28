package se.spacify.db.entity;

import com.j256.ormlite.field.DatabaseField;

import com.j256.ormlite.table.DatabaseTable;

/**
 * A specific audio recording of a MusicWork.
 * Identified globally by ISRC; local playback uses filePath.
 */
@DatabaseTable(tableName = "recordings")
public class Recording extends Content implements Playable {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String title;

    /** International Standard Recording Code. */
    @DatabaseField(unique = true, canBeNull = true)
    private String isrc;

    @DatabaseField
    private long durationMs;

    /** Absolute path to the local audio file, if available. */
    @DatabaseField(canBeNull = true)
    private String filePath;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = true, columnName = "music_work_id")
    private MusicWork musicWork;



    public Recording() {}

    public Recording(String title) { this.title = title; }

    // ── Playable ──────────────────────────────────────────────────────────────

    @Override
    public String getPlayUri() {
        if (filePath != null) return "spacify:local:" + filePath;
        if (isrc    != null) return "spacify:recording:isrc:" + isrc;
        return null;
    }

    @Override public long   getDurationMs() { return durationMs; }
    @Override public String getTitle()      { return title; }

    // ── Getters / setters ─────────────────────────────────────────────────────

    public int       getId()                 { return id; }
    public void      setTitle(String v)      { this.title = v; }
    public String    getIsrc()               { return isrc; }
    public void      setIsrc(String v)       { this.isrc = v; }
    public void      setDurationMs(long v)   { this.durationMs = v; }
    public String    getFilePath()           { return filePath; }
    public void      setFilePath(String v)   { this.filePath = v; }
    public MusicWork getMusicWork()          { return musicWork; }
    public void      setMusicWork(MusicWork v) { this.musicWork = v; }
    
    @Override public String toString() { return title; }
}
