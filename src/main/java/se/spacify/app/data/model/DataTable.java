package se.spacify.app.data.model;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A user-defined custom table (the {@code spacify:table:<slug>} row list). Rows and
 * fields belong to it via {@link DataField#getTableId()} / {@link DataRow#getTableSlug()}.
 * Deletion is soft, like rows, so table history survives in {@code spacify_events}.
 */
@DatabaseTable(tableName = "spacify_data_tables")
public class DataTable {

    @DatabaseField(id = true)
    private String id = UUID.randomUUID().toString();

    @DatabaseField(canBeNull = false, unique = true)
    private String slug;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false, columnName = "created_at")
    private long createdAt;

    @DatabaseField(canBeNull = false, columnName = "updated_at")
    private long updatedAt;

    @DatabaseField(columnName = "deleted_at")
    private Long deletedAt;

    public DataTable() {}

    public String getId()                    { return id; }
    public String getSlug()                  { return slug; }
    public void   setSlug(String v)          { this.slug = v; }
    public String getName()                  { return name; }
    public void   setName(String v)          { this.name = v; }
    public long   getCreatedAt()             { return createdAt; }
    public void   setCreatedAt(long v)       { this.createdAt = v; }
    public long   getUpdatedAt()             { return updatedAt; }
    public void   setUpdatedAt(long v)       { this.updatedAt = v; }
    public Long   getDeletedAt()             { return deletedAt; }
    public void   setDeletedAt(Long v)       { this.deletedAt = v; }
}
