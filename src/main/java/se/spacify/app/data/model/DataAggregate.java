package se.spacify.app.data.model;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A sum/avg rollup attached to a {@link DataRelation}, computed over one NUMBER/FLOAT
 * {@link DataField} of the relation's listed table (the junction table for MANY_TO_MANY,
 * the source table for BELONGS_TO) across whichever rows currently populate that relation's
 * tab. A relation can carry any number of these. See {@code DataRepository#computeAggregates}.
 */
@DatabaseTable(tableName = "spacify_relation_aggregates")
public class DataAggregate {

    @DatabaseField(id = true)
    private String id = UUID.randomUUID().toString();

    @DatabaseField(canBeNull = false, columnName = "relation_id", index = true)
    private String relationId;

    @DatabaseField(canBeNull = false, columnName = "field_id")
    private String fieldId;

    @DatabaseField(canBeNull = false)
    private AggregateKind kind;

    /** Display label; falls back to "Total <field name>"/"Average <field name>" when blank. */
    @DatabaseField
    private String label;

    @DatabaseField(canBeNull = false, columnName = "created_at")
    private long createdAt;

    @DatabaseField(columnName = "deleted_at")
    private Long deletedAt;

    public DataAggregate() {}

    public String        getId()               { return id; }
    public String        getRelationId()       { return relationId; }
    public void          setRelationId(String v) { this.relationId = v; }
    public String        getFieldId()          { return fieldId; }
    public void          setFieldId(String v)   { this.fieldId = v; }
    public AggregateKind getKind()              { return kind; }
    public void          setKind(AggregateKind v) { this.kind = v; }
    public String        getLabel()            { return label; }
    public void          setLabel(String v)     { this.label = v; }
    public long           getCreatedAt()         { return createdAt; }
    public void           setCreatedAt(long v)    { this.createdAt = v; }
    public Long            getDeletedAt()         { return deletedAt; }
    public void            setDeletedAt(Long v)    { this.deletedAt = v; }
}
