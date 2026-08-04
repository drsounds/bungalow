package se.spacify.app.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Singleton pointer (always {@code id == 1}) to the tip of the {@code spacify_events}
 * hash chain, so the last change can be found without scanning the whole log.
 */
@DatabaseTable(tableName = "spacify_events_head")
public class EventsHead {

    public static final int SINGLETON_ID = 1;

    @DatabaseField(id = true)
    private int id = SINGLETON_ID;

    @DatabaseField(columnName = "last_event_id")
    private String lastEventId;

    @DatabaseField(columnName = "last_hash")
    private String lastHash;

    public EventsHead() {}

    public int    getId()                     { return id; }
    public void   setId(int v)                { this.id = v; }
    public String getLastEventId()            { return lastEventId; }
    public void   setLastEventId(String v)    { this.lastEventId = v; }
    public String getLastHash()               { return lastHash; }
    public void   setLastHash(String v)       { this.lastHash = v; }
}
