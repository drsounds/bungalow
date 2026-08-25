package se.spacify.app.data.model;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * One row of a {@link DataTable}. Its cell values live in {@link DataValue} (one
 * table, one row per field per value, keyed EAV-style so unlimited dynamic fields
 * don't need dynamic {@code ALTER TABLE}). {@code created}/{@code updated} are
 * built in; {@code deletedAt} makes deletion soft, per the app's spec.
 */
@DatabaseTable(tableName = "spacify_data_rows")
public class DataRow {

    @DatabaseField(id = true)
    private String id = UUID.randomUUID().toString();

    @DatabaseField(canBeNull = false, columnName = "table_slug", index = true)
    private String tableSlug;

    @DatabaseField(canBeNull = false, columnName = "created_at")
    private long createdAt;

    @DatabaseField(canBeNull = false, columnName = "updated_at")
    private long updatedAt;

    @DatabaseField(columnName = "deleted_at")
    private Long deletedAt;

    @DatabaseField(columnName = "name")
    private String name;

    // No index=true here: unlike table_slug (indexed since the table's original
    // creation), this column is added to an existing table via DatabaseManager's
    // ALTER-TABLE reconciliation, which runs after ORMLite's own createTableIfNotExists
    // — and createTableIfNotExists tries to build any @DatabaseField(index=true)'s index
    // immediately, before the column exists, which throws "no such column: slug".
    @DatabaseField(columnName = "slug")
    private String slug;

    @DatabaseField(canBeNull = false, columnName = "number")
    private long number;

    @DatabaseField(canBeNull = false, columnName = "id_no")
    private long idNo;

    public DataRow() {}

    public String getId()               { return id; }
    public String getTableSlug()        { return tableSlug; }
    public void   setTableSlug(String v) { this.tableSlug = v; }
    public long   getCreatedAt()        { return createdAt; }
    public void   setCreatedAt(long v)  { this.createdAt = v; }
    public long   getUpdatedAt()        { return updatedAt; }
    public void   setUpdatedAt(long v)  { this.updatedAt = v; }
    public Long   getDeletedAt()        { return deletedAt; }
    public void   setDeletedAt(Long v)  { this.deletedAt = v; }
    public String getName()             { return name; }
    public void   setName(String v)     { this.name = v; }
    public String getSlug()             { return slug; }
    public void   setSlug(String v)     { this.slug = v; }
    public long   getNumber()           { return number; }
    public void   setNumber(long v)     { this.number = v; }
    public long   getIdNo()             { return idNo; }
    public void   setIdNo(long v)       { this.idNo = v; }
}
