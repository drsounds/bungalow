package se.spacify.app.data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.j256.ormlite.dao.Dao;

import se.spacify.app.data.model.DataField;
import se.spacify.app.data.model.DataRow;
import se.spacify.app.data.model.DataTable;
import se.spacify.app.data.model.DataValue;
import se.spacify.app.data.model.EventsHead;
import se.spacify.app.data.model.FieldType;
import se.spacify.app.data.model.SpacifyEvent;
import se.spacify.db.DatabaseManager;

/**
 * Persistence and business logic for {@code se.spacify.app.data}: CRUD for custom
 * tables/fields/rows (EAV-backed {@link DataValue} cells, since fields are unlimited
 * and user-defined), soft deletion, related-row resolution via the
 * {@code <slug>_uri}/{@code <slug>_uris} naming convention, and an append-only,
 * hash-chained {@code spacify_events} audit log of every change.
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

    private Dao<DataTable, String>    tables() { return db().dao(DataTable.class, String.class); }
    private Dao<DataField, String>    fields() { return db().dao(DataField.class, String.class); }
    private Dao<DataRow, String>      rows()   { return db().dao(DataRow.class, String.class); }
    private Dao<DataValue, String>    values() { return db().dao(DataValue.class, String.class); }
    private Dao<SpacifyEvent, String> events() { return db().dao(SpacifyEvent.class, String.class); }
    private Dao<EventsHead, Integer>  head()   { return db().dao(EventsHead.class, Integer.class); }

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
     * as the edit-form inputs are named). Values are validated for every field before
     * anything is written, so a bad input never leaves a half-written row.
     */
    public DataRow createRow(DataTable t, List<DataField> tableFields, Map<String, Object> posted) throws SQLException {
        Map<DataField, List<String>> canonical = canonicalizeAll(tableFields, posted);

        DataRow r = new DataRow();
        r.setTableSlug(t.getSlug());
        long now = System.currentTimeMillis();
        r.setCreatedAt(now);
        r.setUpdatedAt(now);
        rows().create(r);
        appendEvent(rowUri(t.getSlug(), r.getId()), "row.created", null, null, null);

        for (Map.Entry<DataField, List<String>> e : canonical.entrySet()) {
            writeValues(t, r, e.getKey(), e.getValue());
        }
        return r;
    }

    public void updateRow(DataTable t, DataRow r, List<DataField> tableFields, Map<String, Object> posted) throws SQLException {
        Map<DataField, List<String>> canonical = canonicalizeAll(tableFields, posted);

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

    // ── Related rows ─────────────────────────────────────────────────────────────

    /**
     * Tables with a LINK field named {@code <base.slug>_uri} or {@code _uris} — i.e.
     * tables whose rows can point back at a row of {@code base} — for the "Related"
     * links on the row detail view.
     */
    public List<DataTable> relatedTablesFor(DataTable base) throws SQLException {
        List<DataField> pointers = fields().queryBuilder().where().isNull("deleted_at")
            .and().in("slug", base.getSlug() + "_uri", base.getSlug() + "_uris").query();
        LinkedHashSet<String> tableSlugs = new LinkedHashSet<>();
        for (DataField f : pointers) {
            if (!f.getTableSlug().equals(base.getSlug())) {
                tableSlugs.add(f.getTableSlug());
            }
        }
        List<DataTable> out = new ArrayList<>();
        for (String slug : tableSlugs) {
            DataTable t = findTable(slug);
            if (t != null) {
                out.add(t);
            }
        }
        return out;
    }

    /** Rows of {@code relatedTable} whose {@code <baseSlug>_uri[s]} field holds {@code rowUri}. */
    public List<DataRow> findRelatedRows(DataTable relatedTable, String baseSlug, String rowUri) throws SQLException {
        List<DataField> pointerFields = fields().queryBuilder().where()
            .eq("table_id", relatedTable.getId()).and().isNull("deleted_at")
            .and().in("slug", baseSlug + "_uri", baseSlug + "_uris").query();
        if (pointerFields.isEmpty()) {
            return List.of();
        }
        Set<String> rowIds = new LinkedHashSet<>();
        for (DataField pf : pointerFields) {
            List<DataValue> matches = values().queryBuilder().where()
                .eq("field_id", pf.getId()).and().eq("value", rowUri).query();
            for (DataValue v : matches) {
                rowIds.add(v.getRowId());
            }
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
