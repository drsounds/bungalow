package se.spacify.app.data.model;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A user-defined relation between two {@link DataTable}s: either {@link RelationKind#BELONGS_TO}
 * (a single LINK {@link DataField}, {@link #getFieldId() fieldId}, on the source table points at
 * a row of the target table) or {@link RelationKind#MANY_TO_MANY} (an ordinary {@link DataTable},
 * {@link #getJunctionTableId() junctionTableId}, acts as junction, with one LINK field per side —
 * {@link #getJunctionSourceFieldId()}/{@link #getJunctionTargetFieldId()}). The junction is not a
 * new storage concept: it's just a regular table, fully CRUD-able like any other, so "special
 * fields on the junction" are just ordinary {@link DataField}s on it.
 *
 * <p>Supersedes the old {@code <targetSlug>_uri}/{@code _uris} field-slug naming convention as
 * the source of relation identity — a relation now identifies its field by id, not by a slug
 * string, so (unlike the old convention) two relations to the same target table from the same
 * source table no longer collide. Pre-existing convention-based fields are backfilled into real
 * rows of this table by {@link se.spacify.app.data.DataRepository#migrateLegacyLinkRelations()}.
 */
@DatabaseTable(tableName = "spacify_relations")
public class DataRelation {

    @DatabaseField(id = true)
    private String id = UUID.randomUUID().toString();

    @DatabaseField(canBeNull = false, index = true)
    private RelationKind kind;

    /** Display label for the relation tab; falls back to the listed table's name when blank. */
    @DatabaseField
    private String name;

    @DatabaseField(canBeNull = false, columnName = "source_table_id", index = true)
    private String sourceTableId;

    @DatabaseField(canBeNull = false, columnName = "source_table_slug")
    private String sourceTableSlug;

    @DatabaseField(canBeNull = false, columnName = "target_table_id", index = true)
    private String targetTableId;

    @DatabaseField(canBeNull = false, columnName = "target_table_slug")
    private String targetTableSlug;

    /** BELONGS_TO only: the LINK field on the source table storing the pointer. */
    @DatabaseField(columnName = "field_id", index = true)
    private String fieldId;

    /** MANY_TO_MANY only: the junction table and its two FK-role LINK fields. */
    @DatabaseField(columnName = "junction_table_id")
    private String junctionTableId;

    @DatabaseField(columnName = "junction_table_slug")
    private String junctionTableSlug;

    @DatabaseField(columnName = "junction_source_field_id")
    private String junctionSourceFieldId;

    @DatabaseField(columnName = "junction_target_field_id")
    private String junctionTargetFieldId;

    // No index=true: unlike the columns above (indexed since this table's original creation),
    // this one was added later via DatabaseManager's ALTER-TABLE reconciliation, which runs
    // after ORMLite's own createTableIfNotExists — an index=true here would make
    // createTableIfNotExists try to build the index before the column exists on an
    // already-populated table (see the identical note on DataRow.slug).
    /** Comma-separated {@link DataField} ids of the listed table's columns to show on this
     *  relation's tab, in order; {@code null} (the default for every relation predating this
     *  column) means "show every field", matching the original, unconfigurable behavior. An
     *  explicit empty string means "show none". */
    @DatabaseField(columnName = "column_field_ids")
    private String columnFieldIds;

    @DatabaseField(canBeNull = false, columnName = "created_at")
    private long createdAt;

    @DatabaseField(columnName = "deleted_at")
    private Long deletedAt;

    public DataRelation() {}

    public String       getId()                        { return id; }
    public RelationKind getKind()                       { return kind; }
    public void         setKind(RelationKind v)          { this.kind = v; }
    public String       getName()                       { return name; }
    public void         setName(String v)                { this.name = v; }
    public String       getSourceTableId()              { return sourceTableId; }
    public void         setSourceTableId(String v)       { this.sourceTableId = v; }
    public String       getSourceTableSlug()            { return sourceTableSlug; }
    public void         setSourceTableSlug(String v)     { this.sourceTableSlug = v; }
    public String       getTargetTableId()              { return targetTableId; }
    public void         setTargetTableId(String v)       { this.targetTableId = v; }
    public String       getTargetTableSlug()            { return targetTableSlug; }
    public void         setTargetTableSlug(String v)     { this.targetTableSlug = v; }
    public String       getFieldId()                    { return fieldId; }
    public void         setFieldId(String v)             { this.fieldId = v; }
    public String       getJunctionTableId()            { return junctionTableId; }
    public void         setJunctionTableId(String v)     { this.junctionTableId = v; }
    public String       getJunctionTableSlug()          { return junctionTableSlug; }
    public void         setJunctionTableSlug(String v)   { this.junctionTableSlug = v; }
    public String       getJunctionSourceFieldId()      { return junctionSourceFieldId; }
    public void         setJunctionSourceFieldId(String v) { this.junctionSourceFieldId = v; }
    public String       getJunctionTargetFieldId()      { return junctionTargetFieldId; }
    public void         setJunctionTargetFieldId(String v) { this.junctionTargetFieldId = v; }
    public String       getColumnFieldIds()             { return columnFieldIds; }
    public void         setColumnFieldIds(String v)      { this.columnFieldIds = v; }
    public long          getCreatedAt()                  { return createdAt; }
    public void          setCreatedAt(long v)             { this.createdAt = v; }
    public Long           getDeletedAt()                  { return deletedAt; }
    public void           setDeletedAt(Long v)             { this.deletedAt = v; }
}
