package se.spacify.app.data;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.j256.ormlite.dao.Dao;

import se.spacify.app.playlist.upsl.Base62;
import se.spacify.app.data.model.AggregateKind;
import se.spacify.app.data.model.DataAggregate;
import se.spacify.app.data.model.DataField;
import se.spacify.app.data.model.DataRelation;
import se.spacify.app.data.model.DataRow;
import se.spacify.app.data.model.DataTable;
import se.spacify.app.data.model.DataValue;
import se.spacify.app.data.model.EventsHead;
import se.spacify.app.data.model.FieldType;
import se.spacify.app.data.model.RelationKind;
import se.spacify.app.data.model.SpacifyEvent;
import se.spacify.db.DatabaseManager;

/**
 * Persistence and business logic for {@code se.spacify.app.data}: CRUD for custom
 * tables/fields/rows (EAV-backed {@link DataValue} cells, since fields are unlimited
 * and user-defined), soft deletion, formal {@link DataRelation belongsTo/many-to-many
 * relation} resolution, and an append-only, hash-chained {@code spacify_events} audit
 * log of every change.
 *
 * <p>Stateless and cheap to construct — {@link DatabaseManager} owns the actual
 * connection/DAO cache — except for {@link #setOnTablesChanged(Runnable)}, a single
 * callback the owning {@code DataApplication} uses to keep the sidebar's table list
 * live; share one instance across the app's controller/view for that to work.
 */
public final class DataRepository {

    private Runnable onTablesChanged;

    /** Notify {@code listener} whenever a table is created or deleted (e.g. to refresh a sidebar). */
    public void setOnTablesChanged(Runnable listener) {
        this.onTablesChanged = listener;
    }

    private static DatabaseManager db() { return DatabaseManager.getInstance(); }

    private Dao<DataTable, String>    tables()   { return db().dao(DataTable.class, String.class); }
    private Dao<DataField, String>    fields()   { return db().dao(DataField.class, String.class); }
    private Dao<DataRow, String>      rows()     { return db().dao(DataRow.class, String.class); }
    private Dao<DataValue, String>    values()   { return db().dao(DataValue.class, String.class); }
    private Dao<DataRelation, String>  relations()  { return db().dao(DataRelation.class, String.class); }
    private Dao<DataAggregate, String> aggregates() { return db().dao(DataAggregate.class, String.class); }
    private Dao<SpacifyEvent, String>  events()    { return db().dao(SpacifyEvent.class, String.class); }
    private Dao<EventsHead, Integer>   head()      { return db().dao(EventsHead.class, Integer.class); }

    /** One relation-derived tab to render on a row's detail page: rows of {@link #listedTable()}
     *  whose {@link #pointerField()} equals the base row's URI. Both relation kinds resolve to
     *  this same shape — for MANY_TO_MANY, {@code listedTable} is the junction table and
     *  {@code pointerField} is whichever of its two FK fields points back at the base table, so
     *  the junction rows themselves (including any "special fields" they carry) are what's
     *  listed, with their other FK field rendering as an ordinary LINK cell/button. Carries the
     *  owning {@link #relation()} so callers can resolve its configured columns/aggregates. */
    public record RelationTab(String label, DataTable listedTable, DataField pointerField, DataRelation relation) {}

    /** One computed sum/avg over a {@link RelationTab}'s current rows, pre-formatted for display. */
    public record AggregateResult(String label, String formattedValue) {}

    /** A row-list filter for the Swing grid: an optional row-name substring plus zero or more
     *  belongsTo-field-equals constraints (one per selected filter dropdown). */
    public record RowFilter(String nameContains, List<FieldEquals> fieldEquals) {
        public record FieldEquals(DataField field, String value) {}
        public static final RowFilter NONE = new RowFilter(null, List.of());
    }

    // ── URIs ─────────────────────────────────────────────────────────────────────

    public String tableUri(String tableSlug)              { return "spacify:table:" + tableSlug; }
    public String rowUri(String tableSlug, String rowId)   { return "spacify:table:" + tableSlug + ":" + rowId; }

    // ── Tables ───────────────────────────────────────────────────────────────────

    public List<DataTable> listTables() throws SQLException {
        List<DataTable> out = tables().queryBuilder().orderBy("name", true)
            .where().isNull("deleted_at").query();
        return out;
    }

    public DataTable findTable(String slug) throws SQLException {
        if (slug == null) return null;
        List<DataTable> found = tables().queryBuilder().where()
            .eq("slug", slug).and().isNull("deleted_at").query();
        return found.isEmpty() ? null : found.get(0);
    }

    private DataTable findAnyTableBySlug(String slug) throws SQLException {
        List<DataTable> found = tables().queryBuilder().where().eq("slug", slug).query();
        return found.isEmpty() ? null : found.get(0);
    }

    public DataTable createTable(String name) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("table name is required");
        }
        String slug = uniqueTableSlug(Slug.of(name, "table"));
        DataTable t = new DataTable();
        t.setSlug(slug);
        t.setName(name.trim());
        long now = System.currentTimeMillis();
        t.setCreatedAt(now);
        t.setUpdatedAt(now);
        tables().create(t);
        appendEvent(tableUri(slug), "table.created", null, null, name.trim());
        notifyTablesChanged();
        return t;
    }

    public DataTable renameTable(DataTable t, String name) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("table name is required");
        }
        t.setName(name.trim());
        t.setUpdatedAt(System.currentTimeMillis());
        tables().update(t);
        appendEvent(tableUri(t.getSlug()), "table.renamed", null, null, name.trim());
        notifyTablesChanged();
        return t;
    }

    public void deleteTable(DataTable t) throws SQLException {
        t.setDeletedAt(System.currentTimeMillis());
        tables().update(t);
        appendEvent(tableUri(t.getSlug()), "table.deleted", null, null, null);
        notifyTablesChanged();
    }

    private String uniqueTableSlug(String base) throws SQLException {
        String slug = base;
        int suffix = 2;
        while (findAnyTableBySlug(slug) != null) {
            slug = base + "-" + (suffix++);
        }
        return slug;
    }

    private void notifyTablesChanged() {
        if (onTablesChanged != null) {
            onTablesChanged.run();
        }
    }

    // ── Fields ───────────────────────────────────────────────────────────────────

    public List<DataField> listFields(DataTable t) throws SQLException {
        return fields().queryBuilder().orderBy("position", true).where()
            .eq("table_id", t.getId()).and().isNull("deleted_at").query();
    }

    public DataField findField(String fieldId) throws SQLException {
        if (fieldId == null) return null;
        return fields().queryForId(fieldId);
    }

    public DataField addField(DataTable t, String name, String rawType) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("field name is required");
        }
        FieldType type = FieldType.parse(rawType);
        String slug = Slug.of(name, "field");
        if (type == FieldType.LINK && !slug.endsWith("_uri") && !slug.endsWith("_uris")) {
            slug = slug + "_uri";
        }
        slug = uniqueFieldSlug(t, slug);
        return persistField(t, slug, name, type);
    }

    /** Persist a new field with an already-computed unique slug — shared by {@link #addField}
     *  and {@link #createLinkField}, which differ only in how the slug is derived. */
    private DataField persistField(DataTable t, String slug, String name, FieldType type) throws SQLException {
        DataField f = new DataField();
        f.setTableId(t.getId());
        f.setTableSlug(t.getSlug());
        f.setSlug(slug);
        f.setName(name.trim());
        f.setType(type);
        f.setPosition(listFields(t).size());
        f.setCreatedAt(System.currentTimeMillis());
        fields().create(f);
        appendEvent(tableUri(t.getSlug()), "field.created", null, f.getId(), name.trim() + ":" + type.name());
        return f;
    }

    public void deleteField(DataField f) throws SQLException {
        f.setDeletedAt(System.currentTimeMillis());
        fields().update(f);
        appendEvent(tableUri(f.getTableSlug()), "field.deleted", null, f.getId(), null);
    }

    private String uniqueFieldSlug(DataTable t, String base) throws SQLException {
        String slug = base;
        int suffix = 2;
        while (fieldSlugTaken(t, slug)) {
            slug = base + "-" + (suffix++);
        }
        return slug;
    }

    private boolean fieldSlugTaken(DataTable t, String slug) throws SQLException {
        return !fields().queryBuilder().where()
            .eq("table_id", t.getId()).and().eq("slug", slug).query().isEmpty();
    }

    // ── Rows ─────────────────────────────────────────────────────────────────────

    public List<DataRow> listRows(DataTable t) throws SQLException {
        return rows().queryBuilder().orderBy("created_at", false).where()
            .eq("table_slug", t.getSlug()).and().isNull("deleted_at").query();
    }

    public DataRow findRow(DataTable t, String rowId) throws SQLException {
        if (rowId == null) return null;
        DataRow r = rows().queryForId(rowId);
        if (r == null || r.getDeletedAt() != null || !r.getTableSlug().equals(t.getSlug())) {
            return null;
        }
        return r;
    }

    /**
     * Create a row and set its cells from {@code posted} (keyed {@code "f_" + field.slug},
     * as the edit-form inputs are named, plus the plain {@code "name"}/{@code "slug"} keys
     * for the row's own standard fields). Values are validated for every field before
     * anything is written, so a bad input never leaves a half-written row.
     */
    public DataRow createRow(DataTable t, List<DataField> tableFields, Map<String, Object> posted) throws SQLException {
        Map<DataField, List<String>> canonical = canonicalizeAll(tableFields, posted);

        DataRow r = persistNewRow(t, textOrNull(posted, "name"), textOrNull(posted, "slug"));
        appendEvent(rowUri(t.getSlug(), r.getId()), "row.created", null, null, null);

        for (Map.Entry<DataField, List<String>> e : canonical.entrySet()) {
            writeValues(t, r, e.getKey(), e.getValue());
        }
        return r;
    }

    /**
     * Build and persist a new row's standard fields: {@code id}/{@code created}/{@code updated}
     * are already handled by {@link DataRow}'s own defaults; here we fill {@code name} (as
     * posted), {@code slug} (as posted, or base62 of the row's UUID {@code id} if left blank),
     * and the shared {@code number}/{@code id_no} auto-increment counter (starting at 1 per
     * table). Synchronized so two concurrent inserts for the same table can't compute the same
     * counter value — the same pattern {@link #appendEvent} uses for its hash chain.
     */
    private synchronized DataRow persistNewRow(DataTable t, String name, String slugInput) throws SQLException {
        DataRow r = new DataRow();
        r.setTableSlug(t.getSlug());
        long now = System.currentTimeMillis();
        r.setCreatedAt(now);
        r.setUpdatedAt(now);
        r.setName(name);
        r.setSlug(slugInput != null ? slugInput : base62Of(r.getId()));
        long seq = rows().queryBuilder().where().eq("table_slug", t.getSlug()).countOf() + 1;
        r.setNumber(seq);
        r.setIdNo(seq);
        rows().create(r);
        return r;
    }

    /** Base62 of a UUID's 128 bits, big-endian. */
    private static String base62Of(String uuid) {
        UUID u = UUID.fromString(uuid);
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(u.getMostSignificantBits());
        bb.putLong(u.getLeastSignificantBits());
        return Base62.encode(bb.array());
    }

    /** A posted value trimmed, or {@code null} if absent/blank. */
    private static String textOrNull(Map<String, Object> posted, String key) {
        Object raw = posted != null ? posted.get(key) : null;
        String s = raw != null ? raw.toString().trim() : "";
        return s.isEmpty() ? null : s;
    }

    public void updateRow(DataTable t, DataRow r, List<DataField> tableFields, Map<String, Object> posted) throws SQLException {
        Map<DataField, List<String>> canonical = canonicalizeAll(tableFields, posted);

        r.setName(textOrNull(posted, "name"));
        r.setUpdatedAt(System.currentTimeMillis());
        rows().update(r);
        for (Map.Entry<DataField, List<String>> e : canonical.entrySet()) {
            writeValues(t, r, e.getKey(), e.getValue());
        }
    }

    public void deleteRow(DataTable t, DataRow r) throws SQLException {
        r.setDeletedAt(System.currentTimeMillis());
        rows().update(r);
        appendEvent(rowUri(t.getSlug(), r.getId()), "row.deleted", null, null, null);
    }

    /** Every field's raw posted value, parsed/validated into its canonical stored form(s). */
    private Map<DataField, List<String>> canonicalizeAll(List<DataField> tableFields, Map<String, Object> posted) {
        Map<DataField, List<String>> out = new java.util.LinkedHashMap<>();
        for (DataField f : tableFields) {
            Object raw = posted != null ? posted.get("f_" + f.getSlug()) : null;
            String text = raw != null ? raw.toString() : "";
            out.put(f, canonicalize(f, text));
        }
        return out;
    }

    /** Parse+validate one field's raw form input into its stored value(s), or throw with a field-scoped message. */
    private List<String> canonicalize(DataField f, String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        try {
            return switch (f.getType()) {
                case TEXT -> List.of(raw.trim());
                case NUMBER -> List.of(Long.toString(Long.parseLong(raw.trim().replace(",", ""))));
                case FLOAT -> List.of(Double.toString(Double.parseDouble(raw.trim().replace(",", ""))));
                case TIMESTAMP -> List.of(Long.toString(parseTimestampInput(raw.trim())));
                case LINK -> f.isMultiValueLink() ? splitCsv(raw) : List.of(raw.trim());
            };
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("field '" + f.getName() + "': " + e.getMessage());
        }
    }

    /** Accepts either an epoch-millis number or a {@code YYYY-MM-DD} date (midnight, system zone). */
    private static long parseTimestampInput(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException notMillis) {
            return LocalDate.parse(raw).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
    }

    private static List<String> splitCsv(String raw) {
        List<String> out = new ArrayList<>();
        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                out.add(trimmed);
            }
        }
        return out;
    }

    /** Replace a row's stored values for one field, logging a single event for the change. */
    private void writeValues(DataTable t, DataRow r, DataField f, List<String> newValues) throws SQLException {
        List<DataValue> existing = values().queryBuilder().where()
            .eq("row_id", r.getId()).and().eq("field_id", f.getId()).query();
        for (DataValue v : existing) {
            values().delete(v);
        }
        int position = 0;
        for (String v : newValues) {
            DataValue dv = new DataValue();
            dv.setRowId(r.getId());
            dv.setFieldId(f.getId());
            dv.setFieldSlug(f.getSlug());
            dv.setPosition(position++);
            dv.setValue(v);
            values().create(dv);
        }
        String joined = String.join(",", newValues);
        String objectUri = f.getType() == FieldType.LINK && !newValues.isEmpty() ? joined : null;
        appendEvent(rowUri(t.getSlug(), r.getId()), f.getSlug(), objectUri, f.getId(),
            newValues.isEmpty() ? null : joined);
    }

    public List<DataValue> valuesFor(String rowId, String fieldId) throws SQLException {
        return values().queryBuilder().orderBy("position", true).where()
            .eq("row_id", rowId).and().eq("field_id", fieldId).query();
    }

    // ── Relations ────────────────────────────────────────────────────────────────

    public DataTable findTableById(String id) throws SQLException {
        if (id == null) return null;
        DataTable t = tables().queryForId(id);
        return (t != null && t.getDeletedAt() == null) ? t : null;
    }

    /**
     * Create a LINK field with an explicit single/multi-value slug suffix, sidestepping
     * {@link #addField}'s implicit convention (which infers cardinality from whatever the
     * typed name happens to already end with). Used by {@link #defineBelongsTo} and
     * {@link #defineManyToMany}; neither creates the {@link DataRelation} itself here —
     * callers do that afterward, once the field exists and has an id to reference.
     */
    private DataField createLinkField(DataTable t, String name, boolean multi) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("field name is required");
        }
        String base = Slug.of(name, "field");
        if (base.endsWith("_uris")) {
            base = base.substring(0, base.length() - "_uris".length());
        } else if (base.endsWith("_uri")) {
            base = base.substring(0, base.length() - "_uri".length());
        }
        String slug = uniqueFieldSlug(t, base + (multi ? "_uris" : "_uri"));
        return persistField(t, slug, name, FieldType.LINK);
    }

    /** Define a belongsTo relation: {@code source} gets a new LINK field (named {@code fieldName},
     *  single- or multi-valued per {@code multi}) pointing at rows of {@code target}. */
    public DataRelation defineBelongsTo(DataTable source, String fieldName, DataTable target,
                                         boolean multi, String relationName) throws SQLException {
        DataField field = createLinkField(source, fieldName, multi);
        return persistBelongsTo(source, target, field, relationName);
    }

    /** Persist a BELONGS_TO relation for an already-existing pointer field — shared by
     *  {@link #defineBelongsTo} (a freshly created field) and {@link #migrateLegacyLinkRelations}
     *  (an existing field formalized after the fact); both log the same {@code relation.created}
     *  event, since either way a first-class {@link DataRelation} row now exists. */
    private DataRelation persistBelongsTo(DataTable source, DataTable target, DataField field, String name)
            throws SQLException {
        DataRelation r = new DataRelation();
        r.setKind(RelationKind.BELONGS_TO);
        r.setName(name);
        r.setSourceTableId(source.getId());
        r.setSourceTableSlug(source.getSlug());
        r.setTargetTableId(target.getId());
        r.setTargetTableSlug(target.getSlug());
        r.setFieldId(field.getId());
        r.setCreatedAt(System.currentTimeMillis());
        relations().create(r);
        appendEvent(tableUri(source.getSlug()), "relation.created", null, r.getId(),
            "belongsTo:" + target.getSlug());
        return r;
    }

    /** Define a many-to-many relation between {@code left} and {@code right}: creates a new
     *  ordinary table named {@code junctionName} with one LINK field per side. Extra "special"
     *  fields on the junction are just ordinary fields added to it afterward via {@link #addField}. */
    public DataRelation defineManyToMany(DataTable left, DataTable right, String junctionName) throws SQLException {
        DataTable junction = createTable(junctionName);
        DataField sourceField = createLinkField(junction, left.getName(), false);
        DataField targetField = createLinkField(junction, right.getName(), false);
        DataRelation r = new DataRelation();
        r.setKind(RelationKind.MANY_TO_MANY);
        r.setSourceTableId(left.getId());
        r.setSourceTableSlug(left.getSlug());
        r.setTargetTableId(right.getId());
        r.setTargetTableSlug(right.getSlug());
        r.setJunctionTableId(junction.getId());
        r.setJunctionTableSlug(junction.getSlug());
        r.setJunctionSourceFieldId(sourceField.getId());
        r.setJunctionTargetFieldId(targetField.getId());
        r.setCreatedAt(System.currentTimeMillis());
        relations().create(r);
        appendEvent(tableUri(junction.getSlug()), "relation.created", null, r.getId(),
            "manyToMany:" + left.getSlug() + "<->" + right.getSlug());
        return r;
    }

    /** Every non-deleted relation touching {@code table}, in creation order: BELONGS_TO relations
     *  where {@code table} is the target (a tab renders there; the source side just edits the
     *  field itself), plus MANY_TO_MANY relations where {@code table} is either side. */
    private List<DataRelation> relationsInvolving(DataTable table) throws SQLException {
        List<DataRelation> asTarget = relations().queryBuilder().where()
            .eq("target_table_id", table.getId()).and().isNull("deleted_at").query();
        List<DataRelation> asSource = relations().queryBuilder().where()
            .eq("source_table_id", table.getId()).and().eq("kind", RelationKind.MANY_TO_MANY)
            .and().isNull("deleted_at").query();
        Map<String, DataRelation> byId = new LinkedHashMap<>();
        for (DataRelation r : asTarget) byId.put(r.getId(), r);
        for (DataRelation r : asSource) byId.put(r.getId(), r);
        List<DataRelation> out = new ArrayList<>(byId.values());
        out.sort(Comparator.comparingLong(DataRelation::getCreatedAt));
        return out;
    }

    /** The relation tabs to render on {@code base}'s row-detail page. */
    public List<RelationTab> relationTabsFor(DataTable base) throws SQLException {
        List<RelationTab> out = new ArrayList<>();
        for (DataRelation r : relationsInvolving(base)) {
            RelationTab tab = toRelationTab(r, base);
            if (tab != null) {
                out.add(tab);
            }
        }
        return out;
    }

    private RelationTab toRelationTab(DataRelation r, DataTable base) throws SQLException {
        DataTable listedTable;
        DataField pointerField;
        if (r.getKind() == RelationKind.BELONGS_TO) {
            listedTable = findTableById(r.getSourceTableId());
            pointerField = findField(r.getFieldId());
        } else {
            boolean baseIsSource = base.getId().equals(r.getSourceTableId());
            listedTable = findTableById(r.getJunctionTableId());
            pointerField = findField(baseIsSource ? r.getJunctionSourceFieldId() : r.getJunctionTargetFieldId());
        }
        if (listedTable == null || pointerField == null || pointerField.getDeletedAt() != null) {
            return null;
        }
        String label = r.getName() != null && !r.getName().isBlank() ? r.getName() : listedTable.getName();
        return new RelationTab(label, listedTable, pointerField, r);
    }

    /** Rows whose {@code pointerField} holds {@code targetRowUri} — the resolution behind every
     *  {@link RelationTab}, keyed by field id rather than the old slug-suffix convention. */
    public List<DataRow> rowsPointingAt(DataField pointerField, String targetRowUri) throws SQLException {
        List<DataValue> matches = values().queryBuilder().where()
            .eq("field_id", pointerField.getId()).and().eq("value", targetRowUri).query();
        LinkedHashSet<String> rowIds = new LinkedHashSet<>();
        for (DataValue v : matches) {
            rowIds.add(v.getRowId());
        }
        List<DataRow> out = new ArrayList<>();
        for (String id : rowIds) {
            DataRow r = rows().queryForId(id);
            if (r != null && r.getDeletedAt() == null) {
                out.add(r);
            }
        }
        return out;
    }

    /** The BELONGS_TO relations owned by {@code table} (i.e. where it holds the pointer field) —
     *  used to render a picker for each such field and to build the Swing grid's filter dropdowns. */
    public List<DataRelation> belongsToRelationsOn(DataTable table) throws SQLException {
        return relations().queryBuilder().where()
            .eq("source_table_id", table.getId()).and().eq("kind", RelationKind.BELONGS_TO)
            .and().isNull("deleted_at").query();
    }

    // ── Relation columns/aggregates (Phase 2) ───────────────────────────────────

    /** The fields of {@code tab}'s listed table to show as columns — every field when the
     *  relation's column selection is unset (the default, matching pre-Phase-2 behavior for
     *  every existing relation), an explicit subset in the configured order when set, or none
     *  at all when explicitly configured to an empty selection. */
    public List<DataField> columnFieldsFor(RelationTab tab) throws SQLException {
        String csv = tab.relation().getColumnFieldIds();
        if (csv == null) {
            return listFields(tab.listedTable());
        }
        if (csv.isBlank()) {
            return List.of();
        }
        List<DataField> out = new ArrayList<>();
        for (String id : csv.split(",")) {
            DataField f = findField(id.trim());
            if (f != null && f.getDeletedAt() == null && tab.listedTable().getId().equals(f.getTableId())) {
                out.add(f);
            }
        }
        return out;
    }

    /** Set {@code relation}'s visible columns to exactly {@code fields} (in the given order), or
     *  to "every field" (the default) when {@code fields} is {@code null}. */
    public void setRelationColumns(DataRelation relation, List<DataField> fields) throws SQLException {
        String csv = fields == null ? null
            : fields.stream().map(DataField::getId).collect(Collectors.joining(","));
        relation.setColumnFieldIds(csv);
        relations().update(relation);
        appendEvent(tableUri(relation.getSourceTableSlug()), "relation.columns.updated", null,
            relation.getId(), csv);
    }

    /** Attach a sum/avg rollup, over {@code field} (must be NUMBER or FLOAT), to {@code relation}'s tab. */
    public DataAggregate defineAggregate(DataRelation relation, DataField field, AggregateKind kind, String label)
            throws SQLException {
        if (field.getType() != FieldType.NUMBER && field.getType() != FieldType.FLOAT) {
            throw new IllegalArgumentException("aggregate field must be number or float");
        }
        DataAggregate a = new DataAggregate();
        a.setRelationId(relation.getId());
        a.setFieldId(field.getId());
        a.setKind(kind);
        a.setLabel(label);
        a.setCreatedAt(System.currentTimeMillis());
        aggregates().create(a);
        appendEvent(tableUri(relation.getSourceTableSlug()), "aggregate.created", null, a.getId(),
            kind.name() + ":" + field.getSlug());
        return a;
    }

    /** The non-deleted aggregates attached to {@code relation}, in creation order. */
    public List<DataAggregate> aggregatesFor(DataRelation relation) throws SQLException {
        return aggregates().queryBuilder().orderBy("created_at", true).where()
            .eq("relation_id", relation.getId()).and().isNull("deleted_at").query();
    }

    /** Compute every aggregate attached to {@code tab}'s relation over {@code rows} (the same
     *  rows already resolved for that tab, so this doesn't re-query them) — skips an aggregate
     *  whose field was since deleted, and a row with no value for the field simply doesn't count
     *  toward the sum/average rather than being treated as zero. */
    public List<AggregateResult> computeAggregates(RelationTab tab, List<DataRow> rows) throws SQLException {
        List<AggregateResult> out = new ArrayList<>();
        for (DataAggregate a : aggregatesFor(tab.relation())) {
            DataField field = findField(a.getFieldId());
            if (field == null || field.getDeletedAt() != null) {
                continue;
            }
            double sum = 0;
            int count = 0;
            for (DataRow r : rows) {
                List<DataValue> values = valuesFor(r.getId(), field.getId());
                if (values.isEmpty()) {
                    continue;
                }
                try {
                    sum += Double.parseDouble(values.get(0).getValue());
                    count++;
                } catch (NumberFormatException ignored) {
                    // A malformed stored value just doesn't count toward the rollup.
                }
            }
            boolean isAvg = a.getKind() == AggregateKind.AVG;
            double value = isAvg ? (count > 0 ? sum / count : 0) : sum;
            String formatted = (!isAvg && field.getType() == FieldType.NUMBER)
                ? Format.number((long) value) : Format.decimal(value);
            String label = a.getLabel() != null && !a.getLabel().isBlank() ? a.getLabel()
                : (isAvg ? "Average " : "Total ") + field.getName();
            out.add(new AggregateResult(label, formatted));
        }
        return out;
    }

    /**
     * Best-effort, idempotent backfill: for every non-multi LINK field whose slug still follows
     * the old {@code <targetSlug>_uri} convention and has no {@link DataRelation} yet, formalize
     * it into one. Metadata-only — {@link DataValue} cell data is never touched, so this is safe
     * to run on every startup. A field that doesn't cleanly match the convention (multi-value, or
     * its target slug no longer resolves to a real table) is left exactly as it behaves today: a
     * plain, unmanaged LINK field with free-text input and no relation tab.
     */
    public void migrateLegacyLinkRelations() throws SQLException {
        // Every field id already spoken for by an existing relation — as a BELONGS_TO pointer
        // or as either side of a MANY_TO_MANY junction — must be skipped: a many-to-many
        // junction's own FK fields are ordinary "<targetSlug>_uri"-looking LINK fields too
        // (createLinkField names them after the table they point at), so without this check
        // every M:N relation would grow a spurious duplicate BELONGS_TO relation (and a
        // duplicate tab) for each of its two junction fields on every activation.
        Set<String> referencedFieldIds = new HashSet<>();
        for (DataRelation r : relations().queryBuilder().query()) {
            if (r.getFieldId() != null) referencedFieldIds.add(r.getFieldId());
            if (r.getJunctionSourceFieldId() != null) referencedFieldIds.add(r.getJunctionSourceFieldId());
            if (r.getJunctionTargetFieldId() != null) referencedFieldIds.add(r.getJunctionTargetFieldId());
        }
        for (DataField f : fields().queryBuilder().where()
                .eq("type", FieldType.LINK).and().isNull("deleted_at").query()) {
            if (f.isMultiValueLink() || !f.getSlug().endsWith("_uri") || referencedFieldIds.contains(f.getId())) {
                continue;
            }
            String targetSlug = f.getSlug().substring(0, f.getSlug().length() - "_uri".length());
            DataTable target = findTable(targetSlug);
            DataTable source = findTable(f.getTableSlug());
            if (target == null || source == null) {
                continue;
            }
            persistBelongsTo(source, target, f, null);
        }
    }

    // ── Row search / filtering ──────────────────────────────────────────────────

    /** Rows of {@code table} matching {@code filter}'s name substring and field-equals constraints. */
    public List<DataRow> searchRows(DataTable table, RowFilter filter) throws SQLException {
        List<DataRow> out = new ArrayList<>();
        for (DataRow r : listRows(table)) {
            if (filter.nameContains() != null && !nameContains(r, filter.nameContains())) {
                continue;
            }
            if (!fieldEqualsAllMatch(r, filter.fieldEquals())) {
                continue;
            }
            out.add(r);
        }
        return out;
    }

    private static boolean nameContains(DataRow r, String needle) {
        String name = r.getName();
        return name != null && name.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private boolean fieldEqualsAllMatch(DataRow r, List<RowFilter.FieldEquals> constraints) throws SQLException {
        for (RowFilter.FieldEquals c : constraints) {
            boolean matched = false;
            for (DataValue v : valuesFor(r.getId(), c.field().getId())) {
                if (c.value().equals(v.getValue())) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    // ── Event log (hash chain) ──────────────────────────────────────────────────

    private synchronized void appendEvent(String subjectUri, String predicate, String objectUri,
                                           String fieldId, String value) throws SQLException {
        EventsHead h = head().queryForId(EventsHead.SINGLETON_ID);
        if (h == null) {
            h = new EventsHead();
        }
        SpacifyEvent e = new SpacifyEvent();
        e.setTimestamp(System.currentTimeMillis());
        e.setSubjectUri(subjectUri);
        e.setPredicate(predicate);
        e.setObjectUri(objectUri);
        e.setFieldId(fieldId);
        e.setValue(value);
        e.setPrevHash(h.getLastHash());
        e.setHash(hashOf(h.getLastHash(), e));
        events().create(e);

        h.setLastEventId(e.getId());
        h.setLastHash(e.getHash());
        head().createOrUpdate(h);
    }

    private static String hashOf(String prevHash, SpacifyEvent e) {
        String payload = String.join("|",
            orEmpty(prevHash), Long.toString(e.getTimestamp()), orEmpty(e.getSubjectUri()),
            orEmpty(e.getPredicate()), orEmpty(e.getObjectUri()), orEmpty(e.getFieldId()), orEmpty(e.getValue()));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private static String orEmpty(String s) { return s != null ? s : ""; }
}
