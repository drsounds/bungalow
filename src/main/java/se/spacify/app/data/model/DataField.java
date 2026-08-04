package se.spacify.app.data.model;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A column defined on a {@link DataTable}: unlimited per table, each with a
 * {@link FieldType}. A {@code LINK} field's {@link #getSlug()} must end {@code _uri}
 * (one value) or {@code _uris} (many) — {@link se.spacify.app.data.DataRepository}
 * uses that suffix both to render hyperlinks and to resolve the
 * {@code spacify:table:<slug>:<row_id>:<related slug>} related-rows view (a related
 * table's row is "related" when its {@code <slug>_uri[s]} field holds this row's URI).
 */
@DatabaseTable(tableName = "spacify_data_fields")
public class DataField {

    @DatabaseField(id = true)
    private String id = UUID.randomUUID().toString();

    @DatabaseField(canBeNull = false, columnName = "table_id", index = true, uniqueCombo = true)
    private String tableId;

    /** Denormalised so field lookups don't need a join back to {@link DataTable}. */
    @DatabaseField(canBeNull = false, columnName = "table_slug", index = true)
    private String tableSlug;

    /** Unique per table (enforced together with {@link #tableId} via {@code uniqueCombo}). */
    @DatabaseField(canBeNull = false, uniqueCombo = true)
    private String slug;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false)
    private FieldType type;

    /** Display/column order within the table. */
    @DatabaseField(canBeNull = false)
    private int position;

    @DatabaseField(canBeNull = false, columnName = "created_at")
    private long createdAt;

    @DatabaseField(columnName = "deleted_at")
    private Long deletedAt;

    public DataField() {}

    public String    getId()                 { return id; }
    public String    getTableId()             { return tableId; }
    public void      setTableId(String v)     { this.tableId = v; }
    public String    getTableSlug()           { return tableSlug; }
    public void      setTableSlug(String v)   { this.tableSlug = v; }
    public String    getSlug()                { return slug; }
    public void      setSlug(String v)        { this.slug = v; }
    public String    getName()                { return name; }
    public void      setName(String v)        { this.name = v; }
    public FieldType getType()                { return type; }
    public void      setType(FieldType v)     { this.type = v; }
    public int       getPosition()            { return position; }
    public void      setPosition(int v)       { this.position = v; }
    public long      getCreatedAt()           { return createdAt; }
    public void      setCreatedAt(long v)     { this.createdAt = v; }
    public Long      getDeletedAt()           { return deletedAt; }
    public void      setDeletedAt(Long v)     { this.deletedAt = v; }

    /** Whether this LINK field holds many URIs (slug ends {@code _uris}) rather than one ({@code _uri}). */
    public boolean isMultiValueLink() {
        return type == FieldType.LINK && slug != null && slug.endsWith("_uris");
    }
}
