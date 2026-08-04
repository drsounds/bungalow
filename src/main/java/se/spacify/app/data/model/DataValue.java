package se.spacify.app.data.model;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * One scalar value of a {@link DataRow}'s {@link DataField}. A single-value field
 * has exactly one row here ({@code position == 0}); a multi-value LINK field
 * (slug ending {@code _uris}) has one row per URI, ordered by {@link #getPosition()}.
 * Storing cells this way (rather than dynamic columns) is what lets a table have an
 * unlimited, user-defined set of fields on top of plain SQLite tables.
 */
@DatabaseTable(tableName = "spacify_data_values")
public class DataValue {

    @DatabaseField(id = true)
    private String id = UUID.randomUUID().toString();

    @DatabaseField(canBeNull = false, columnName = "row_id", index = true)
    private String rowId;

    @DatabaseField(canBeNull = false, columnName = "field_id", index = true)
    private String fieldId;

    /** Denormalised so related-row scans can filter by slug without joining {@link DataField}. */
    @DatabaseField(canBeNull = false, columnName = "field_slug")
    private String fieldSlug;

    @DatabaseField(canBeNull = false)
    private int position;

    @DatabaseField(canBeNull = true, width = 65535)
    private String value;

    public DataValue() {}

    public String getId()                  { return id; }
    public String getRowId()               { return rowId; }
    public void   setRowId(String v)       { this.rowId = v; }
    public String getFieldId()             { return fieldId; }
    public void   setFieldId(String v)     { this.fieldId = v; }
    public String getFieldSlug()           { return fieldSlug; }
    public void   setFieldSlug(String v)   { this.fieldSlug = v; }
    public int    getPosition()            { return position; }
    public void   setPosition(int v)       { this.position = v; }
    public String getValue()               { return value; }
    public void   setValue(String v)       { this.value = v; }
}
