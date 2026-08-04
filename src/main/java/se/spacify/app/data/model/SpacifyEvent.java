package se.spacify.app.data.model;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * One entry in the {@code spacify_events} append-only log: every change to a custom
 * table's definition (fields) or data (rows/values) is recorded here as a subject /
 * predicate / object triple, in addition to being applied to the live tables.
 *
 * <ul>
 *   <li>{@code subjectUri} — the {@code spacify:table:...} URI of the thing changed
 *       (a table or a row).</li>
 *   <li>{@code predicate} — what changed: a lifecycle name ({@code table.created},
 *       {@code field.created}, {@code row.deleted}, ...) or, for a cell edit, the
 *       field's slug.</li>
 *   <li>{@code objectUri} — for a LINK field edit, the URI value(s) written (comma-joined
 *       if multi-valued); null otherwise.</li>
 *   <li>{@code fieldId} — the {@link DataField} touched, when applicable.</li>
 *   <li>{@code value} — the raw new value recorded for the change.</li>
 * </ul>
 *
 * <p>{@code prevHash}/{@code hash} hash-chain the log (each event's hash covers its
 * own content plus the previous event's hash), so the sequence is tamper-evident;
 * {@link EventsHead} is the pointer to the chain's current tip.
 */
@DatabaseTable(tableName = "spacify_events")
public class SpacifyEvent {

    @DatabaseField(id = true)
    private String id = UUID.randomUUID().toString();

    @DatabaseField(canBeNull = false)
    private long timestamp;

    @DatabaseField(canBeNull = false, columnName = "subject_uri")
    private String subjectUri;

    @DatabaseField(canBeNull = false)
    private String predicate;

    @DatabaseField(columnName = "object_uri")
    private String objectUri;

    @DatabaseField(columnName = "field_id")
    private String fieldId;

    @DatabaseField(width = 65535)
    private String value;

    @DatabaseField(columnName = "prev_hash")
    private String prevHash;

    @DatabaseField(canBeNull = false)
    private String hash;

    public SpacifyEvent() {}

    public String getId()                    { return id; }
    public long   getTimestamp()             { return timestamp; }
    public void   setTimestamp(long v)       { this.timestamp = v; }
    public String getSubjectUri()            { return subjectUri; }
    public void   setSubjectUri(String v)    { this.subjectUri = v; }
    public String getPredicate()             { return predicate; }
    public void   setPredicate(String v)     { this.predicate = v; }
    public String getObjectUri()             { return objectUri; }
    public void   setObjectUri(String v)     { this.objectUri = v; }
    public String getFieldId()               { return fieldId; }
    public void   setFieldId(String v)       { this.fieldId = v; }
    public String getValue()                 { return value; }
    public void   setValue(String v)         { this.value = v; }
    public String getPrevHash()              { return prevHash; }
    public void   setPrevHash(String v)      { this.prevHash = v; }
    public String getHash()                  { return hash; }
    public void   setHash(String v)          { this.hash = v; }
}
